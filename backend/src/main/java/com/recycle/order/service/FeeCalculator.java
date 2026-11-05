package com.recycle.order.service;

import com.recycle.order.dto.OrderQuoteReq;
import com.recycle.order.vo.FeeDetailVO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 运价计算器（PRD 7.1 计价规则）。
 *
 * <p>所有费率集中在这里，后续接入配置中心或数据库配置表时只改这一个类。
 */
@Component
public class FeeCalculator {

    /** 起步价：含 3 公里 */
    private static final BigDecimal BASE_FEE = new BigDecimal("30");
    private static final BigDecimal BASE_KM = new BigDecimal("3");
    /** 超出部分每公里 */
    private static final BigDecimal PER_KM_FEE = new BigDecimal("2");

    /** 体积档：上限（不含）→ 费用 */
    private static final BigDecimal VOLUME_SMALL = new BigDecimal("0.3");
    private static final BigDecimal VOLUME_MEDIUM = new BigDecimal("1");
    private static final BigDecimal VOLUME_LARGE = new BigDecimal("3");

    /** 楼层费：无电梯时，2 楼起每层，单件封顶 */
    private static final BigDecimal FLOOR_FEE_PER_LEVEL = new BigDecimal("10");
    private static final BigDecimal FLOOR_FEE_CAP = new BigDecimal("50");

    /** 平台服务费率 */
    private static final BigDecimal SERVICE_RATE = new BigDecimal("0.05");

    /** 拆装费 */
    private static final Map<String, BigDecimal> ASSEMBLY_FEE = Map.of(
            "bed", new BigDecimal("100"),
            "wardrobe", new BigDecimal("120"),
            "sofa", new BigDecimal("50"),
            "none", BigDecimal.ZERO
    );

    public FeeDetailVO compute(BigDecimal itemPrice, OrderQuoteReq req) {
        BigDecimal deliveryFee = BigDecimal.ZERO;
        BigDecimal floorFee = BigDecimal.ZERO;
        BigDecimal assemblyFee = BigDecimal.ZERO;
        String note = "同城自提，无需运费";

        if (Integer.valueOf(2).equals(req.getDeliveryType())) {
            deliveryFee = deliveryFee(req.getDistanceKm(), req.getVolumeM3());
            floorFee = floorFee(req.getFloor(), Boolean.TRUE.equals(req.getHasElevator()));
            assemblyFee = ASSEMBLY_FEE.getOrDefault(
                    req.getAssemblyType() == null ? "none" : req.getAssemblyType(), BigDecimal.ZERO);
            note = "平台配送，含运费与搬运服务";
        }

        BigDecimal serviceFee = itemPrice.multiply(SERVICE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = itemPrice.add(deliveryFee).add(floorFee).add(assemblyFee).add(serviceFee);

        return new FeeDetailVO(req.getItemId(), itemPrice, deliveryFee, floorFee,
                assemblyFee, serviceFee, total.setScale(2, RoundingMode.HALF_UP), note);
    }

    /**
     * 运费 = 起步价（含 3km）+ 超出里程 × 2 元/km + 体积档
     */
    BigDecimal deliveryFee(BigDecimal distanceKm, BigDecimal volumeM3) {
        BigDecimal distance = distanceKm == null ? BigDecimal.ZERO : distanceKm;
        BigDecimal fee = BASE_FEE;

        if (distance.compareTo(BASE_KM) > 0) {
            BigDecimal extraKm = distance.subtract(BASE_KM).setScale(0, RoundingMode.UP);
            fee = fee.add(extraKm.multiply(PER_KM_FEE));
        }
        return fee.add(volumeFee(volumeM3));
    }

    BigDecimal volumeFee(BigDecimal volumeM3) {
        if (volumeM3 == null) {
            return BigDecimal.ZERO;
        }
        if (volumeM3.compareTo(VOLUME_SMALL) < 0) {
            return BigDecimal.ZERO;
        }
        if (volumeM3.compareTo(VOLUME_MEDIUM) < 0) {
            return new BigDecimal("30");
        }
        if (volumeM3.compareTo(VOLUME_LARGE) < 0) {
            return new BigDecimal("80");
        }
        return new BigDecimal("150");
    }

    /**
     * 无电梯时，2 楼起每上一层 10 元，封顶 50 元。
     */
    BigDecimal floorFee(Integer floor, boolean hasElevator) {
        if (hasElevator || floor == null || floor <= 1) {
            return BigDecimal.ZERO;
        }
        BigDecimal fee = FLOOR_FEE_PER_LEVEL.multiply(BigDecimal.valueOf(floor - 1L));
        return fee.min(FLOOR_FEE_CAP);
    }
}
