/*
 * Copyright (c) 2018. paascloud.net All Rights Reserved.
 * 项目名称：paascloud快速搭建企业级分布式微服务平台
 * 类名称：UacUserMainController.java
 * 创建人：刘兆明
 * 联系方式：paascloud.net@gmail.com
 * 开源地址: https://github.com/paascloud
 * 博客地址: http://blog.paascloud.net
 * 项目官网: http://paascloud.net
 */

package com.paascloud.provider.web;

import com.github.pagehelper.PageInfo;
import com.paascloud.Md5Util;
import com.paascloud.PublicUtil;
import com.paascloud.base.dto.LoginAuthDto;
import com.paascloud.base.enums.ErrorCodeEnum;
import com.paascloud.core.support.BaseController;
import com.paascloud.provider.model.domain.UacUser;
import com.paascloud.provider.model.dto.menu.UserMenuDto;
import com.paascloud.provider.model.dto.user.*;
import com.paascloud.provider.model.exceptions.UacBizException;
import com.paascloud.provider.model.service.UacUserFeignApi;
import com.paascloud.provider.model.vo.menu.MenuVo;
import com.paascloud.provider.model.vo.role.RoleVo;
import com.paascloud.provider.model.vo.role.UserBindRoleVo;
import com.paascloud.provider.model.vo.user.UserVo;
import com.paascloud.provider.service.UacRoleService;
import com.paascloud.provider.service.UacUserService;
import com.paascloud.wrapper.WrapMapper;
import com.paascloud.wrapper.Wrapper;
import io.swagger.annotations.Api;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;


/**
 * 用户管理主页面.
 *
 * @author paascloud.net @gmail.com
 */
@RefreshScope
@RestController
@Api(value = "API - UacUserFeignClient", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UacUserFeignClient extends BaseController implements UacUserFeignApi {

	@Resource
	private UacUserService uacUserService;

	@Resource
	private UacRoleService uacRoleService;

	/**
	 * 查询角色列表.
	 *
	 * @param uacUser the uac user
	 *
	 * @return the wrapper
	 */
	@Override
	public Wrapper<PageInfo> queryUserListWithPage(@RequestBody UserInfoDto uacUser) {
		logger.info("查询用户列表uacUser={}", uacUser);
		UacUser user = new UacUser();
		BeanUtils.copyProperties(user,uacUser);
		PageInfo pageInfo = uacUserService.queryUserListWithPage(user);
		return WrapMapper.ok(pageInfo);
	}

	/**
	 * 新增用户
	 *
	 * @param user the user
	 *
	 * @return the wrapper
	 */
	@Override
	public Wrapper<Integer> addUacUser(@RequestBody UserInfoDto user) {
		logger.info(" 新增用户 user={}", user);
		LoginAuthDto loginAuthDto = user.getLoginAuthDto();
		UacUser uacUser = new UacUser();
		BeanUtils.copyProperties(user,uacUser);
		uacUserService.saveUacUser(uacUser, loginAuthDto);
		return WrapMapper.ok();
	}

	/**
	 * 根据Id修改用户状态.
	 *
	 * @param modifyUserStatusDto the modify user status dto
	 *
	 * @return the wrapper
	 */
	@Override
	public Wrapper<Integer> modifyUserStatusById(@RequestBody ModifyUserStatusDto modifyUserStatusDto) {
		logger.info(" 根据Id修改用户状态 modifyUserStatusDto={}", modifyUserStatusDto);
		LoginAuthDto loginAuthDto = modifyUserStatusDto.getLoginAuthDto();
		UacUser uacUser = new UacUser();
		uacUser.setId(modifyUserStatusDto.getUserId());
		uacUser.setStatus(modifyUserStatusDto.getStatus());

		int result = uacUserService.modifyUserStatusById(uacUser, loginAuthDto);
		return WrapMapper.handleResult(result);
	}

	/**
	 * 通过Id删除用户.
	 *
	 * @param userId the user id
	 *
	 * @return the wrapper
	 */
	@Override
	public Wrapper<Integer> deleteUserById(@PathVariable("userId") Long userId) {
		logger.info(" 通过Id删除用户 userId={}", userId);
		int result = uacUserService.deleteUserById(userId);
		return WrapMapper.handleResult(result);
	}

	/**
	 * 获取用户绑定角色页面数据.
	 *
	 * @param userId the user id
	 *
	 * @return the bind role
	 */
	@Override
	public Wrapper<UserBindRoleVo> getBindRole(@PathVariable("userId") Long userId) {
		logger.info("获取用户绑定角色页面数据. userId={}", userId);
		//FIXME
		LoginAuthDto loginAuthDto = null;// super.getLoginAuthDto();
		Long currentUserId = loginAuthDto.getUserId();
		if (Objects.equals(userId, currentUserId)) {
			throw new UacBizException(ErrorCodeEnum.UAC10011023);
		}

		UserBindRoleVo bindUserDto = uacUserService.getUserBindRoleDto(userId);
		return WrapMapper.ok(bindUserDto);
	}

	/**
	 * 用户绑定角色.
	 *
	 * @param bindUserRolesDto the bind user roles dto
	 *
	 * @return the wrapper
	 */
	@Override
	public Wrapper<Integer> bindUserRoles(@RequestBody BindUserRolesDto bindUserRolesDto) {
		logger.info("用户绑定角色 bindUserRolesDto={}", bindUserRolesDto);
		LoginAuthDto loginAuthDto = bindUserRolesDto.getLoginAuthDto();
		uacUserService.bindUserRoles(bindUserRolesDto, loginAuthDto);
		return WrapMapper.ok();
	}

	/**
	 * 查询用户常用功能数据.
	 *
	 * @return the wrapper
	 */
	@Override
	public Wrapper<List<UserMenuDto>> queryUserMenuDtoData() {
		logger.info("查询用户常用功能数据");
		//FIXME
		LoginAuthDto loginAuthDto = null; // getLoginAuthDto();
		List<UserMenuDto> userMenuDtoList = uacUserService.queryUserMenuDtoData(loginAuthDto);
		return WrapMapper.ok(userMenuDtoList);
	}

	/**
	 * 绑定用户常用菜单.
	 *
	 * @param bindUserMenusDto the bind user menus dto
	 *
	 * @return the wrapper
	 */
	@Override
	public Wrapper<Integer> bindUserMenus(@RequestBody BindUserMenusDto bindUserMenusDto) {
		logger.info("绑定用户常用菜单");
		List<Long> menuIdList = bindUserMenusDto.getMenuIdList();
		logger.info("menuIdList = {}", menuIdList);

		int result = uacUserService.bindUserMenus(menuIdList, bindUserMenusDto.getLoginAuthDto());

		return WrapMapper.handleResult(result);
	}

	/**
	 * 根据用户Id查询用户信息.
	 *
	 * @param userId the user id
	 *
	 * @return the uac user by id
	 */
	@Override
	public Wrapper<UserVo> getUacUserById(@PathVariable("userId") Long userId) {
		logger.info("getUacUserById - 根据用户Id查询用户信息. userId={}", userId);
		UacUser uacUser = uacUserService.queryByUserId(userId);
		logger.info("getUacUserById - 根据用户Id查询用户信息. [OK] uacUser={}", uacUser);
		UserVo userVo = new UserVo();
		BeanUtils.copyProperties(uacUser,userVo);
		return WrapMapper.ok(userVo);
	}

	/**
	 * 根据用户Id重置密码.
	 *
	 * @param userId the user id
	 *
	 * @return the wrapper
	 */
	@Override
	public Wrapper<UserVo> resetLoginPwd(@PathVariable("userId") Long userId) {
		logger.info("resetLoginPwd - 根据用户Id重置密码. userId={}", userId);
		//FIXME
//		uacUserService.resetLoginPwd(userId, getLoginAuthDto());
		return WrapMapper.ok();
	}

	/**
	 * 根据userId查询用户详细信息（连表查询）.
	 *
	 * @return the wrapper
	 */
	@Override
	public Wrapper<UserVo> queryUserInfo(@PathVariable("loginName") String loginName) {
		logger.info("根据userId查询用户详细信息");
		UserVo userVo = new UserVo();
		UacUser uacUser = uacUserService.findByLoginName(loginName);
		uacUser = uacUserService.findUserInfoByUserId(uacUser.getId());
		List<RoleVo> roleList = uacRoleService.findAllRoleInfoByUserId(uacUser.getId());
		List<MenuVo> authTree = uacRoleService.getOwnAuthTree(uacUser.getId());
		BeanUtils.copyProperties(uacUser, userVo);
		if (PublicUtil.isNotEmpty(roleList)) {
			userVo.setRoles(new HashSet<>(roleList));
		}
		userVo.setAuthTree(authTree);
		return WrapMapper.ok(userVo);
	}

	/**
	 * 校验登录名唯一性.
	 *
	 * @param checkLoginNameDto the check login name dto
	 *
	 * @return the wrapper
	 */
	@Override
	public Wrapper<Boolean> checkLoginName(@RequestBody CheckLoginNameDto checkLoginNameDto) {
		logger.info("校验登录名唯一性 checkLoginNameDto={}", checkLoginNameDto);

		Long id = checkLoginNameDto.getUserId();
		String loginName = checkLoginNameDto.getLoginName();

		Example example = new Example(UacUser.class);
		Example.Criteria criteria = example.createCriteria();
		criteria.andEqualTo("loginName", loginName);
		if (id != null) {
			criteria.andNotEqualTo("id", id);
		}

		int result = uacUserService.selectCountByExample(example);
		return WrapMapper.ok(result < 1);
	}

	/**
	 * 校验登录名唯一性.
	 *
	 * @param checkEmailDto the check email dto
	 *
	 * @return the wrapper
	 */
	@Override
	public Wrapper<Boolean> checkEmail(@RequestBody CheckEmailDto checkEmailDto) {
		logger.info("校验邮箱唯一性 checkEmailDto={}", checkEmailDto);

		Long id = checkEmailDto.getUserId();
		String email = checkEmailDto.getEmail();

		Example example = new Example(UacUser.class);
		Example.Criteria criteria = example.createCriteria();
		criteria.andEqualTo("email", email);
		if (id != null) {
			criteria.andNotEqualTo("id", id);
		}

		int result = uacUserService.selectCountByExample(example);
		return WrapMapper.ok(result < 1);
	}

	/**
	 * 校验真实姓名唯一性.
	 *
	 * @param checkUserNameDto the check user name dto
	 *
	 * @return the wrapper
	 */
	@Override
	public Wrapper<Boolean> checkUserName(@RequestBody CheckUserNameDto checkUserNameDto) {
		logger.info(" 校验真实姓名唯一性 checkUserNameDto={}", checkUserNameDto);
		Long id = checkUserNameDto.getUserId();
		String name = checkUserNameDto.getUserName();

		Example example = new Example(UacUser.class);
		Example.Criteria criteria = example.createCriteria();
		criteria.andEqualTo("userName", name);
		if (id != null) {
			criteria.andNotEqualTo("id", id);
		}

		int result = uacUserService.selectCountByExample(example);
		return WrapMapper.ok(result < 1);
	}

	/**
	 * 校验用户电话号码唯一性.
	 *
	 * @param checkUserPhoneDto the check user phone dto
	 *
	 * @return the wrapper
	 */
	@Override
	public Wrapper<Boolean> checkUserPhone(@RequestBody CheckUserPhoneDto checkUserPhoneDto) {
		logger.info(" 校验用户电话号码唯一性 checkUserPhoneDto={}", checkUserPhoneDto);
		Long id = checkUserPhoneDto.getUserId();
		String mobileNo = checkUserPhoneDto.getMobileNo();
		Example example = new Example(UacUser.class);
		Example.Criteria criteria = example.createCriteria();
		criteria.andEqualTo("mobileNo", mobileNo);

		if (id != null) {
			criteria.andNotEqualTo("id", id);
		}

		int result = uacUserService.selectCountByExample(example);
		return WrapMapper.ok(result < 1);
	}

	/**
	 * 校验新密码是否与原始密码相同.
	 *
	 * @param checkNewPasswordDto 修改密码实体
	 *
	 * @return the wrapper
	 */
	@Override
	public Wrapper<Boolean> checkNewPassword(@RequestBody CheckNewPasswordDto checkNewPasswordDto) {
		logger.info(" 校验新密码是否与原始密码相同 checkNewPasswordDto={}", checkNewPasswordDto);
		String loginName = checkNewPasswordDto.getLoginName();
		String newPassword = checkNewPasswordDto.getNewPassword();
		UacUser uacUser = new UacUser();
		uacUser.setLoginName(loginName);
		int result = 0;
		UacUser user = uacUserService.findByLoginName(loginName);
		if (user == null) {
			logger.error("找不到用户. loginName={}", loginName);
		} else {
			uacUser.setLoginPwd(Md5Util.encrypt(newPassword));
			result = uacUserService.selectCount(uacUser);
		}
		return WrapMapper.ok(result < 1);
	}


	/**
	 * 修改用户邮箱
	 *
	 * @param email     the email
	 * @param emailCode the email code
	 *
	 * @return the wrapper
	 */
	@Override
	public Wrapper<Integer> modifyUserEmail(@PathVariable("email") String email, @PathVariable("emailCode") String emailCode) {
		logger.info(" 修改用户信息 email={}, emailCode={}", email, emailCode);
		// FIXME
//		LoginAuthDto loginAuthDto = getLoginAuthDto();
//		uacUserService.modifyUserEmail(email, emailCode, loginAuthDto);
		return WrapMapper.ok();
	}

	/**
	 * 获取已有权限树
	 *
	 * @return the auth tree by role id
	 */
	@Override
	public Wrapper<List<MenuVo>> getOwnAuthTree() {
	    // FIXME
		List<MenuVo> tree = null; // uacRoleService.getOwnAuthTree(getLoginAuthDto().getUserId());
		return WrapMapper.ok(tree);
	}

	/**
	 * 用户修改密码
	 *
	 * @param userModifyPwdDto the user modify pwd dto
	 *
	 * @return the wrapper
	 */
	@Override
	public Wrapper<Integer> modifyUserPwd(@RequestBody UserModifyPwdDto userModifyPwdDto) {
		logger.info("==》vue用户修改密码, userModifyPwdDto={}", userModifyPwdDto);

		logger.info("旧密码 oldPassword = {}", userModifyPwdDto.getOldPassword());
		logger.info("新密码 newPassword = {}", userModifyPwdDto.getNewPassword());
		logger.info("登录名 loginName = {}", userModifyPwdDto.getLoginName());

		LoginAuthDto loginAuthDto = userModifyPwdDto.getLoginAuthDto();

		int result = uacUserService.userModifyPwd(userModifyPwdDto, loginAuthDto);
		return WrapMapper.handleResult(result);
	}

	/**
	 * 注册
	 *
	 * @param registerDto the register dto
	 *
	 * @return the wrapper
	 */
	@Override
	public Wrapper registerUser(@RequestBody UserRegisterDto registerDto) {
		logger.info("vue注册开始。注册参数：{}", registerDto);
		uacUserService.register(registerDto);
		return WrapMapper.ok("注册成功");
	}
}
