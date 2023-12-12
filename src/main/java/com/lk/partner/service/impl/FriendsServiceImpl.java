package com.lk.partner.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.lk.partner.common.ErrorCode;
import com.lk.partner.contant.FriendConstant;
import com.lk.partner.exception.BusinessException;
import com.lk.partner.mapper.FriendsMapper;
import com.lk.partner.model.entity.Friends;
import com.lk.partner.model.entity.User;
import com.lk.partner.model.request.FriendAddRequest;
import com.lk.partner.model.vo.FriendsRecordVO;
import com.lk.partner.service.FriendsService;
import com.lk.partner.service.UserService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author lk
 * @description 针对表【friends(好友申请管理表)】的数据库操作Service实现
 * @createDate 2023-04-17 09:28:08
 */
@Service
public class FriendsServiceImpl extends ServiceImpl<FriendsMapper, Friends> implements FriendsService {
    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public boolean addFriendRecords(User loginUser, FriendAddRequest friendAddRequest) {
        if (StringUtils.isNotBlank(friendAddRequest.getRemark()) && friendAddRequest.getRemark().length() > 120) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "申请备注最多120个字符");
        }
        if (ObjectUtils.anyNull(loginUser.getId(), friendAddRequest.getReceiveId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "添加失败");
        }
        // 1.添加的不能是自己
        if (loginUser.getId() == friendAddRequest.getReceiveId()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能添加自己为好友");
        }
        RLock lock = redissonClient.getLock("partner:apply");
        try {
            // 抢到锁并执行
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                // 2.条数大于等于1 就不能再添加
                LambdaQueryWrapper<Friends> friendsLambdaQueryWrapper = new LambdaQueryWrapper<>();
                friendsLambdaQueryWrapper.eq(Friends::getReceiveId, friendAddRequest.getReceiveId());
                friendsLambdaQueryWrapper.eq(Friends::getFromId, loginUser.getId());
                List<Friends> list = this.list(friendsLambdaQueryWrapper);
                list.forEach(friends -> {
                    if (list.size() > 1 && friends.getStatus() == FriendConstant.DEFAULT_STATUS) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能重复申请");
                    }
                });
                Friends newFriend = new Friends();
                newFriend.setFromId(loginUser.getId());
                newFriend.setReceiveId(friendAddRequest.getReceiveId());
                if (StringUtils.isBlank(friendAddRequest.getRemark())) {
                    newFriend.setRemark("我是" + userService.getById(loginUser.getId()).getUsername());
                } else {
                    newFriend.setRemark(friendAddRequest.getRemark());
                }
                newFriend.setCreateTime(new Date());
                return this.save(newFriend);
            }
        } catch (InterruptedException e) {
            log.error("joinTeam error", e);
            return false;
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
        return false;
    }


    @Override
    public List<FriendsRecordVO> obtainFriendApplicationRecords(User loginUser) {
        // 查询出当前用户所有申请、同意记录
        LambdaQueryWrapper<Friends> friendsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        friendsLambdaQueryWrapper.eq(Friends::getReceiveId, loginUser.getId());
        return toFriendsVo(friendsLambdaQueryWrapper);
    }

    private List<FriendsRecordVO> toFriendsVo(LambdaQueryWrapper<Friends> friendsLambdaQueryWrapper) {
        // 根据friendsLambdaQueryWrapper查询friendsList
        List<Friends> friendsList = this.list(friendsLambdaQueryWrapper);
        // 将friendsList反转
        Collections.reverse(friendsList);
        // 遍历friendsList，将friend转换为FriendsRecordVO
        return friendsList.stream().map(friend -> {
            FriendsRecordVO friendsRecordVO = new FriendsRecordVO();
            // 使用BeanUtils将friend的属性值复制到friendsRecordVO
            BeanUtils.copyProperties(friend, friendsRecordVO);
            // 根据friend.getFromId查询用户
            User user = userService.getById(friend.getFromId());
            // 将查询到的用户转换为安全用户
            friendsRecordVO.setApplyUser(userService.getSafetyUser(user));
            return friendsRecordVO;
        }).collect(Collectors.toList());
    }


    @Override
    public List<FriendsRecordVO> getMyRecords(User loginUser) {
        // 查询出当前用户所有申请、同意记录
        LambdaQueryWrapper<Friends> myApplyLambdaQueryWrapper = new LambdaQueryWrapper<>();
        myApplyLambdaQueryWrapper.eq(Friends::getFromId, loginUser.getId());
        List<Friends> friendsList = this.list(myApplyLambdaQueryWrapper);
        Collections.reverse(friendsList);
        return friendsList.stream().map(friend -> {
            FriendsRecordVO friendsRecordVO = new FriendsRecordVO();
            BeanUtils.copyProperties(friend, friendsRecordVO);
            User user = userService.getById(friend.getReceiveId());
            friendsRecordVO.setApplyUser(userService.getSafetyUser(user));
            return friendsRecordVO;
        }).collect(Collectors.toList());
    }

    @Override
    public int getRecordCount(User loginUser) {
        // 创建LambdaQueryWrapper对象
        LambdaQueryWrapper<Friends> friendsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 设置查询条件
        friendsLambdaQueryWrapper.eq(Friends::getReceiveId, loginUser.getId());
        // 执行查询
        List<Friends> friendsList = this.list(friendsLambdaQueryWrapper);
        // 定义计数变量
        int count = 0;
        // 遍历查询结果
        for (Friends friend : friendsList) {
            // 如果状态为默认状态，且未读状态为未读，则计数加1
            if (friend.getStatus() == FriendConstant.DEFAULT_STATUS && friend.getIsRead() == FriendConstant.NOT_READ) {
                count++;
            }
        }
        // 返回计数结果
        return count;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toRead(User loginUser, Set<Long> ids) {
        boolean flag = false;
        for (Long id : ids) {
            Friends friend = this.getById(id);
            if (friend.getStatus() == FriendConstant.DEFAULT_STATUS && friend.getIsRead() == FriendConstant.NOT_READ) {
                friend.setIsRead(FriendConstant.READ);
                flag = this.updateById(friend);
            }
        }
        return flag;
    }

    @Override
    public boolean agreeToApply(User loginUser, Long fromId) {
        // 0. 根据receiveId查询所有接收的申请记录
        LambdaQueryWrapper<Friends> friendsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        friendsLambdaQueryWrapper.eq(Friends::getReceiveId, loginUser.getId());
        friendsLambdaQueryWrapper.eq(Friends::getFromId, fromId);
        List<Friends> recordCount = this.list(friendsLambdaQueryWrapper);
        List<Friends> collect = recordCount.stream().filter(f -> f.getStatus() == FriendConstant.DEFAULT_STATUS).collect(Collectors.toList());
        // 条数小于1 就不能再同意
        if (collect.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该申请不存在");
        }
        if (collect.size() > 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "操作有误,请重试");
        }
        AtomicBoolean flag = new AtomicBoolean(false);
        collect.forEach(friend -> {
            if (DateUtil.between(new Date(), friend.getCreateTime(), DateUnit.DAY) >= 3 || friend.getStatus() == FriendConstant.EXPIRED_STATUS) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该申请已过期");
            }
            // 1. 分别查询receiveId和fromId的用户，更改userIds中的数据
            User receiveUser = userService.getById(loginUser.getId());
            User fromUser = userService.getById(fromId);
            Set<Long> receiveUserIds = com.lk.partner.utils.StringUtils.stringJsonListToLongSet(receiveUser.getUserIds());
            Set<Long> fromUserUserIds = com.lk.partner.utils.StringUtils.stringJsonListToLongSet(fromUser.getUserIds());

            fromUserUserIds.add(receiveUser.getId());
            receiveUserIds.add(fromUser.getId());

            Gson gson = new Gson();
            String jsonFromUserUserIds = gson.toJson(fromUserUserIds);
            String jsonReceiveUserIds = gson.toJson(receiveUserIds);
            receiveUser.setUserIds(jsonReceiveUserIds);
            fromUser.setUserIds(jsonFromUserUserIds);
            // 2. 修改状态由0改为1
            friend.setStatus(FriendConstant.AGREE_STATUS);
            flag.set(userService.updateById(fromUser) && userService.updateById(receiveUser) && this.updateById(friend));
        });
        return flag.get();
    }

    @Override
    public boolean canceledApply(Long id, User loginUser) {
        Friends friend = this.getById(id);
        if (friend.getStatus() != FriendConstant.DEFAULT_STATUS) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该申请已过期或已通过");
        }
        friend.setStatus(FriendConstant.REVOKE_STATUS);
        return this.updateById(friend);
    }
}



