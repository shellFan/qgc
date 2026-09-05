package com.qiongguichou.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiongguichou.wallet.entity.Wallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface WalletMapper extends BaseMapper<Wallet> {

    /**
     * 原子更新余额(乐观锁)
     */
    @Update("UPDATE qgc_user_wallet SET balance = balance + #{delta}, " +
            "total_income = total_income + CASE WHEN #{delta} > 0 THEN #{delta} ELSE 0 END, " +
            "total_withdraw = total_withdraw + CASE WHEN #{delta} < 0 THEN ABS(#{delta}) ELSE 0 END, " +
            "version = version + 1, update_time = NOW() " +
            "WHERE user_id = #{userId} AND version = #{version}")
    int updateBalance(@Param("userId") Long userId, @Param("delta") Long delta, @Param("version") Integer version);
}