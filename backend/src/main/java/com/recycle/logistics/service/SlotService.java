package com.recycle.logistics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.recycle.common.exception.BizException;
import com.recycle.logistics.domain.LogisticsSlot;
import com.recycle.logistics.mapper.LogisticsSlotMapper;
import com.recycle.logistics.vo.SlotVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 配送时段与容量。
 *
 * <p>容量用「条件更新占位」控制，不依赖锁：
 * {@code UPDATE ... SET booked = booked + 1 WHERE id = ? AND booked < capacity}，
 * 受影响行数为 0 即说明约满。
 */
@Service
public class SlotService {

    /** 时段定义：编号 → 起始小时 */
    private static final List<String[]> SLOTS = List.of(
            new String[]{"09-12", "9"},
            new String[]{"12-15", "12"},
            new String[]{"15-18", "15"},
            new String[]{"18-21", "18"}
    );

    private static final int DEFAULT_CAPACITY = 5;
    /** 可预约天数：今天 + 未来 7 天 */
    public static final int BOOKABLE_DAYS = 7;

    private final LogisticsSlotMapper slotMapper;

    public SlotService(LogisticsSlotMapper slotMapper) {
        this.slotMapper = slotMapper;
    }

    /**
     * 查询某天可预约时段；该日首次被查询时按默认容量生成记录。
     */
    public List<SlotVO> list(LocalDate date) {
        checkDateRange(date);
        ensureSlots(date);

        List<LogisticsSlot> rows = slotMapper.selectList(
                new LambdaQueryWrapper<LogisticsSlot>()
                        .eq(LogisticsSlot::getSlotDate, date)
                        .orderByAsc(LogisticsSlot::getStartHour));

        LocalDateTime now = LocalDateTime.now();
        List<SlotVO> result = new ArrayList<>(rows.size());
        for (LogisticsSlot row : rows) {
            // 时段结束（起始时间 + 3 小时）前都还能约；已开始但未结束的会立即派单
            boolean ended = date.equals(now.toLocalDate()) && now.getHour() >= row.getStartHour() + 3;
            int remaining = Math.max(0, row.getCapacity() - row.getBooked());
            result.add(new SlotVO(row.getSlotCode(), row.getSlotCode() + " 点",
                    row.getCapacity(), row.getBooked(), remaining, remaining > 0 && !ended));
        }
        return result;
    }

    public LogisticsSlot get(LocalDate date, String slotCode) {
        return slotMapper.selectOne(new LambdaQueryWrapper<LogisticsSlot>()
                .eq(LogisticsSlot::getSlotDate, date)
                .eq(LogisticsSlot::getSlotCode, slotCode));
    }

    /**
     * 占一个名额，约满返回 false。
     */
    public boolean lock(Long slotId) {
        return slotMapper.update(null, new LambdaUpdateWrapper<LogisticsSlot>()
                .eq(LogisticsSlot::getId, slotId)
                .apply("booked < capacity")
                .setSql("booked = booked + 1")) > 0;
    }

    /**
     * 释放一个名额（改约时）。
     */
    public void release(Long slotId) {
        slotMapper.update(null, new LambdaUpdateWrapper<LogisticsSlot>()
                .eq(LogisticsSlot::getId, slotId)
                .apply("booked > 0")
                .setSql("booked = booked - 1"));
    }

    public void checkDateRange(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date == null) {
            throw new BizException("请选择配送日期");
        }
        if (date.isBefore(today)) {
            throw new BizException("不能选择过去的日期");
        }
        if (date.isAfter(today.plusDays(BOOKABLE_DAYS))) {
            throw new BizException("最多只能预约未来 " + BOOKABLE_DAYS + " 天");
        }
    }

    public int startHourOf(String slotCode) {
        for (String[] s : SLOTS) {
            if (s[0].equals(slotCode)) {
                return Integer.parseInt(s[1]);
            }
        }
        throw new BizException("时段不合法");
    }

    private void ensureSlots(LocalDate date) {
        Long count = slotMapper.selectCount(
                new LambdaQueryWrapper<LogisticsSlot>().eq(LogisticsSlot::getSlotDate, date));
        if (count != null && count > 0) {
            return;
        }
        for (String[] s : SLOTS) {
            LogisticsSlot slot = new LogisticsSlot();
            slot.setSlotDate(date);
            slot.setSlotCode(s[0]);
            slot.setStartHour(Integer.parseInt(s[1]));
            slot.setCapacity(DEFAULT_CAPACITY);
            slot.setBooked(0);
            slotMapper.insert(slot);
        }
    }
}
