/*
 * Copyright (c) 2018. paascloud.net All Rights Reserved.
 * 项目名称：paascloud快速搭建企业级分布式微服务平台
 * 类名称：UacRoleMainController.java
 * 创建人：刘兆明
 * 联系方式：paascloud.net@gmail.com
 * 开源地址: https://github.com/paascloud
 * 博客地址: http://blog.paascloud.net
 * 项目官网: http://paascloud.net
 */

package com.paascloud.provider.web;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.paascloud.base.dto.LoginAuthDto;
import com.paascloud.base.enums.ErrorCodeEnum;
import com.paascloud.core.support.BaseController;
import com.paascloud.core.utils.RequestUtil;
import com.paascloud.provider.model.domain.UacRole;
import com.paascloud.provider.model.domain.UacRoleUser;
import com.paascloud.provider.model.dto.base.ModifyStatusDto;
import com.paascloud.provider.model.dto.role.*;
import com.paascloud.provider.model.enums.UacRoleStatusEnum;
import com.paascloud.provider.model.exceptions.UacBizException;
import com.paascloud.provider.model.service.UacRoleFeignApi;
import com.paascloud.provider.model.vo.menu.BindAuthVo;
import com.paascloud.provider.model.vo.role.RoleVo;
import com.paascloud.provider.service.UacRoleService;
import com.paascloud.provider.service.UacRoleUserService;
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
import java.util.List;


/**
 * 角色管理主页面.
 *
 * @author paascloud.net @gmail.com
 */
@RefreshScope
@RestController
@Api(value = "API - UacRoleFeignClient", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UacRoleFeignClient extends BaseController implements UacRoleFeignApi {

    @Resource
    private UacRoleService uacRoleService;

    @Resource
    private UacRoleUserService uacRoleUserService;

    /**
     * 分页查询角色信息.
     *
     * @param role the role
     *
     * @return the wrapper
     */
    @Override
    public Wrapper<PageInfo<RoleVo>> queryUacRoleListWithPage(@RequestBody RoleDto role) {
        logger.info("查询角色列表roleQuery={}", role);
        PageHelper.startPage(role.getPageNum(), role.getPageSize());
        role.setOrderBy("update_time desc");
        List<RoleVo> roleVoList = uacRoleService.queryRoleListWithPage(role);
        return WrapMapper.ok(new PageInfo<>(roleVoList));
    }

    /**
     * 删除角色信息.
     *
     * @param id the id
     *
     * @return the wrapper
     */
    @Override
    public Wrapper deleteUacRoleById(@PathVariable("id") Long id) {
        int result = uacRoleService.deleteRoleById(id);
        return WrapMapper.handleResult(result);
    }

    /**
     * 批量删除角色.
     *
     * @param deleteIdList the delete id list
     *
     * @return the wrapper
     */
    @Override
    public Wrapper batchDeleteByIdList(@RequestBody List<Long> deleteIdList) {
        logger.info("批量删除角色 idList={}", deleteIdList);
        uacRoleService.batchDeleteByIdList(deleteIdList);
        return WrapMapper.ok();
    }

    /**
     * 修改角色状态.
     *
     * @param modifyStatusDto the modify status dto
     *
     * @return the wrapper
     */
    @Override
    public Wrapper modifyUacRoleStatusById(@RequestBody ModifyStatusDto modifyStatusDto) {
        logger.info("根据角色Id修改角色状态 modifyStatusDto={}", modifyStatusDto);
        Long roleId = modifyStatusDto.getId();
        if (roleId == null) {
            throw new UacBizException(ErrorCodeEnum.UAC10012001);
        }

        LoginAuthDto loginAuthDto = modifyStatusDto.getLoginAuthDto();
        Long userId = loginAuthDto.getUserId();

        UacRoleUser ru = uacRoleUserService.getByUserIdAndRoleId(userId, roleId);

        if (ru != null && UacRoleStatusEnum.DISABLE.getType().equals(modifyStatusDto.getStatus())) {
            throw new UacBizException(ErrorCodeEnum.UAC10012002);
        }

        UacRole uacRole = new UacRole();
        uacRole.setId(roleId);
        uacRole.setStatus(modifyStatusDto.getStatus());
        uacRole.setUpdateInfo(loginAuthDto);

        int result = uacRoleService.update(uacRole);
        return WrapMapper.handleResult(result);
    }


    /**
     * 保存角色.
     *
     * @param role the role
     *
     * @return the wrapper
     */
    @Override
    public Wrapper save(@RequestBody RoleDto role) {
        LoginAuthDto loginAuthDto = role.getLoginAuthDto();
        UacRole uacRole = new UacRole();
        BeanUtils.copyProperties(role,uacRole);
        uacRoleService.saveRole(uacRole, loginAuthDto);
        return WrapMapper.ok();
    }

    /**
     * 角色分配权限.
     *
     * @param roleBindActionDto the role bind action dto
     *
     * @return the wrapper
     */
    @Override
    public Wrapper bindAction(@RequestBody RoleBindActionDto roleBindActionDto) {
        logger.info("角色分配权限. roleBindActionDto= {}", roleBindActionDto);
        uacRoleService.bindAction(roleBindActionDto);
        return WrapMapper.ok();
    }

    /**
     * 角色绑定菜单.
     *
     * @param roleBindMenuDto the role bind menu dto
     *
     * @return the wrapper
     */
    @Override
    public Wrapper bindMenu(@RequestBody RoleBindMenuDto roleBindMenuDto) {
        logger.info("角色绑定菜单. roleBindMenuDto= {}", roleBindMenuDto);
        uacRoleService.bindMenu(roleBindMenuDto);
        return WrapMapper.ok();
    }

    /**
     * 角色绑定用户.
     *
     * @param roleBindUserReqDto the role bind user req dto
     *
     * @return the wrapper
     */
    @Override
    public Wrapper bindUser(@RequestBody RoleBindUserReqDto roleBindUserReqDto) {
        logger.info("roleBindUser={}", roleBindUserReqDto);
        LoginAuthDto loginAuthDto = roleBindUserReqDto.getLoginAuthDto();
        uacRoleService.bindUser4Role(roleBindUserReqDto, loginAuthDto);
        return WrapMapper.ok();
    }

    /**
     * 获取角色绑定用户页面数据.
     *
     * @param roleId the role id
     *
     * @return the wrapper
     */
    @Override
    public Wrapper<RoleBindUserDto> getBindUser(@PathVariable("id") Long roleId) {
        logger.info("获取角色绑定用户页面数据. roleId={}", roleId);
        // FIXME 修改
        LoginAuthDto loginAuthDto = null;// super.getLoginAuthDto();
        Long currentUserId = loginAuthDto.getUserId();
        RoleBindUserDto bindUserDto = uacRoleService.getRoleBindUserDto(roleId, currentUserId);
        return WrapMapper.ok(bindUserDto);
    }

    /**
     * 查看角色信息.
     *
     * @param id the id
     *
     * @return the wrapper
     */
    @Override
    public Wrapper<RoleVo> queryRoleInfo(@PathVariable("id") Long id) {
        UacRole role = uacRoleService.selectByKey(id);
        RoleVo roleVo = new RoleVo();
        BeanUtils.copyProperties(role,roleVo);
        return WrapMapper.wrap(Wrapper.SUCCESS_CODE, Wrapper.SUCCESS_MESSAGE, roleVo);
    }


    /**
     * 验证角色编码是否存在.
     *
     * @param checkRoleCodeDto the check role code dto
     *
     * @return the wrapper
     */
    @Override
    public Wrapper<Boolean> checkUacRoleCode(@RequestBody CheckRoleCodeDto checkRoleCodeDto) {

        logger.info("校验角色编码唯一性 checkRoleCodeDto={}", checkRoleCodeDto);

        Long id = checkRoleCodeDto.getRoleId();
        String roleCode = checkRoleCodeDto.getRoleCode();

        Example example = new Example(UacRole.class);
        Example.Criteria criteria = example.createCriteria();

        if (id != null) {
            criteria.andNotEqualTo("id", id);
        }
        criteria.andEqualTo("roleCode", roleCode);

        int result = uacRoleService.selectCountByExample(example);
        return WrapMapper.ok(result < 1);
    }

    /**
     * 获取权限树
     *
     * @param roleId the role id
     *
     * @return the auth tree by role id
     */
    @Override
    public Wrapper<BindAuthVo> getActionTreeByRoleId(@PathVariable("roleId") Long roleId) {
        logger.info("roleId={}", roleId);
        BindAuthVo bindAuthVo = uacRoleService.getActionTreeByRoleId(roleId);
        return WrapMapper.ok(bindAuthVo);
    }

    /**
     * 获取菜单树.
     *
     * @param roleId the role id
     *
     * @return the menu tree by role id
     */
    @Override
    public Wrapper<BindAuthVo> getMenuTreeByRoleId(@PathVariable("roleId") Long roleId) {
        logger.info("roleId={}", roleId);
        BindAuthVo bindAuthVo = uacRoleService.getMenuTreeByRoleId(roleId);
        return WrapMapper.ok(bindAuthVo);
    }
}
