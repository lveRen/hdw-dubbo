package com.hdw.system.service.impl;

import com.hdw.common.base.service.impl.BaseServiceImpl;
import com.hdw.common.constants.CommonConstants;
import com.hdw.common.exception.GlobalException;
import com.hdw.system.entity.SysRole;
import com.hdw.system.entity.vo.RoleVo;
import com.hdw.system.mapper.SysRoleMapper;
import com.hdw.system.service.ISysRoleResourceService;
import com.hdw.system.service.ISysRoleService;
import com.hdw.system.service.ISysUserRoleService;
import com.hdw.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 角色表
 *
 * @author TuMinglong
 * @date 2018-12-11 11:35:15
 */
@Slf4j
@Service(interfaceName = "ISysRoleService")
@Transactional(rollbackFor = Exception.class)
public class SysRoleServiceImpl extends BaseServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private ISysRoleResourceService roleResourceService;
    @Autowired
    private ISysUserRoleService userRoleService;

    @Override
    public List<SysRole> selectSysRoleList(Map<String, Object> param) {
        return this.baseMapper.selectSysRoleList(param);
    }

    @Override
    public RoleVo selectByUserId(Long userId) {
        return this.baseMapper.selectByUserId(userId);
    }

    @Override
    public RoleVo selectByRoleId(Long roleId) {
        return this.baseMapper.selectByRoleId(roleId);
    }

    @Override
    public void saveByVo(SysRole role) {
        this.save(role);
        //检查权限是否越权
        checkPrems(role);
        //保存角色与菜单关系
        roleResourceService.saveOrUpdateRoleResource(role.getId(), role.getResourceIdList());
    }

    @Override
    public void updateByVo(SysRole role) {
        this.updateById(role);
        //检查权限是否越权
        checkPrems(role);
        //更新角色与菜单关系
        roleResourceService.saveOrUpdateRoleResource(role.getId(), role.getResourceIdList());

    }

    @Override
    public void deleteBatch(Long[] roleIds) {
        //删除角色
        this.removeByIds(Arrays.asList(roleIds));

        //删除角色与菜单关联
        roleResourceService.deleteBatch(roleIds);

        //删除角色与用户关联
        userRoleService.deleteBatchByRoleIds(roleIds);
    }

    /**
     * 检查权限是否越权
     */
    private void checkPrems(SysRole role) {
        //如果不是超级管理员，则需要判断角色的权限是否超过自己的权限
        if (role.getCreateUserId() == CommonConstants.SUPER_ADMIN) {
            return;
        }
        //查询用户所拥有的菜单列表
        List<Long> resourceIdList = sysUserService.selectResourceIdListByUserId(role.getCreateUserId());
        //判断是否越权
        if (!resourceIdList.containsAll(role.getResourceIdList())) {
            throw new GlobalException("新增角色的权限，已超出你的权限范围");
        }
    }
}
