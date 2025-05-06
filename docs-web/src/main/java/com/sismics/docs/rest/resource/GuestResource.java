package com.sismics.docs.rest.resource;

import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.PendingUserDao;
import com.sismics.docs.core.dao.criteria.UserCriteria;
import com.sismics.docs.core.dao.dto.UserDto;
import com.sismics.docs.core.model.jpa.PendingUser;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.util.ValidationUtil;
import at.favre.lib.crypto.bcrypt.BCrypt;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Path("/guest")
public class GuestResource extends BaseResource {

    @POST
    @Path("register")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response register(
            @FormParam("username") String username,
            @FormParam("email") String email,
            @FormParam("password") String password) {
        ValidationUtil.validateRequired(username, "username");
        ValidationUtil.validateRequired(email, "email");
        ValidationUtil.validateRequired(password, "password");

        // 检查用户名或邮箱是否已存在
        UserDao userDao = new UserDao();
        if (userDao.getActiveByUsername(username) != null) {
            throw new ClientException("UsernameAlreadyExists", "用户名已注册");
        }
        // 检查邮箱
        if (checkEmailExists(email)) {
            throw new ClientException("EmailAlreadyExists", "邮箱已注册");
        }

        // 保存待审批用户
        PendingUser pendingUser = new PendingUser();
        pendingUser.setId(UUID.randomUUID().toString());
        pendingUser.setUsername(username);
        pendingUser.setEmail(email);
        // // 直接使用 BCrypt 哈希密码
        // String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        pendingUser.setPassword(password);
        pendingUser.setRequestDate(new Date());

        PendingUserDao pendingUserDao = new PendingUserDao();
        pendingUserDao.create(pendingUser);

        return Response.ok().entity(Json.createObjectBuilder()
                .add("status", "请求已提交").build()).build();
    }

    @GET
    @Path("pending_users")
    public Response getPendingUsers() {
        if (!isAdmin()) {
            throw new ClientException("Forbidden", "需要管理员权限");
        }

        PendingUserDao pendingUserDao = new PendingUserDao();
        List<PendingUser> pendingUsers = pendingUserDao.findAll();

        JsonArrayBuilder array = Json.createArrayBuilder();
        for (PendingUser pu : pendingUsers) {
            array.add(Json.createObjectBuilder()
                    .add("id", pu.getId())
                    .add("username", pu.getUsername())
                    .add("email", pu.getEmail())
                    .add("request_date", pu.getRequestDate().getTime()));
        }

        return Response.ok().entity(Json.createObjectBuilder()
                .add("pending_users", array.build()).build()).build();
    }

    @POST
    @Path("approve_user")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response approveUser(@FormParam("id") String id) {
        if (!isAdmin()) {
            throw new ClientException("Forbidden", "需要管理员权限");
        }

        PendingUserDao pendingUserDao = new PendingUserDao();
        PendingUser pendingUser = pendingUserDao.getById(id);
        if (pendingUser == null) {
            throw new ClientException("NotFound", "未找到待审批用户");
        }

        System.out.println("Approving user: " + pendingUser.getUsername() + ", email: " + pendingUser.getEmail() + ", password: " + pendingUser.getPassword());

        // 创建新用户
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(pendingUser.getUsername());
        user.setEmail(pendingUser.getEmail());
        user.setPassword(pendingUser.getPassword());
        user.setRoleId("user");
        user.setCreateDate(new Date());
        user.setStorageQuota(0L);
        user.setStorageCurrent(0L);

        UserDao userDao = new UserDao();
        try {
            userDao.create(user, principal.getId()); // 使用 principal 获取当前用户 ID
        } catch (Exception e) {
            throw new ClientException("UserCreationFailed", "创建用户失败：" + e.getMessage());
        }

        // 删除待审批记录
        pendingUserDao.delete(id);

        return Response.ok().entity(Json.createObjectBuilder()
                .add("status", "用户已通过").build()).build();
    }

    @POST
    @Path("reject_user")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response rejectUser(@FormParam("id") String id) {
        if (!isAdmin()) {
            throw new ClientException("Forbidden", "需要管理员权限");
        }

        PendingUserDao pendingUserDao = new PendingUserDao();
        PendingUser pendingUser = pendingUserDao.getById(id);
        if (pendingUser == null) {
            throw new ClientException("NotFound", "未找到待审批用户");
        }

        pendingUserDao.delete(id);

        return Response.ok().entity(Json.createObjectBuilder()
                .add("status", "用户已拒绝").build()).build();
    }

    private boolean isAdmin() {
        // 确保已认证并获取 principal
        if (!authenticate()) {
            return false;
        }
        return hasBaseFunction(BaseFunction.ADMIN); // 使用 BaseResource 的 hasBaseFunction 检查权限
    }

    private boolean checkEmailExists(String email) {
        UserDao userDao = new UserDao();
        List<UserDto> users = userDao.findByCriteria(new UserCriteria(), null);
        for (UserDto user : users) {
            if (email.equalsIgnoreCase(user.getEmail())) {
                return true;
            }
        }
        return false;
    }
}