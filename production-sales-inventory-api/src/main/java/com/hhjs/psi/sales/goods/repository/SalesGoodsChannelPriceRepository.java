package com.hhjs.psi.sales.goods.repository;

import com.hhjs.psi.sales.goods.entity.SalesGoodsChannelPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalesGoodsChannelPriceRepository extends JpaRepository<SalesGoodsChannelPrice, Long> {

    Optional<SalesGoodsChannelPrice> findFirstBySalesGoodsIdAndChannelIdAndEnabledTrue(Long salesGoodsId, Long channelId);
}
