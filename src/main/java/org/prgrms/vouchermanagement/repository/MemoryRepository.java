package org.prgrms.vouchermanagement.repository;

import org.prgrms.vouchermanagement.exception.InvalidRangeException;
import org.prgrms.vouchermanagement.voucher.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("local")
public class MemoryRepository implements VoucherRepository{

    private final Map<UUID, Voucher> storage = new ConcurrentHashMap<>();


    //의존관계를 직접 관리하면 생기는 유연하지 않는 부분? -> 스프링의 이점
    //영속화하는 레포지토리
    //service
    @Override
    public void create(UUID voucherId, long amountOrPercent, PolicyStatus policy) {
        DiscountPolicy discountPolicy = null;
        if (policy == PolicyStatus.FIXED) {
            discountPolicy = new FixedAmountVoucher(voucherId, amountOrPercent, policy);
        } else if (policy == PolicyStatus.PERCENT) {
            validateAmountOrPercentRange(amountOrPercent);
            discountPolicy = new PercentDiscountVoucher(voucherId, amountOrPercent, policy);
        }

        Voucher voucher = new Voucher(voucherId, discountPolicy);
        storage.put(voucherId, voucher);
    }

    @Override
    public Voucher getById(UUID voucherId) {
        return storage.get(voucherId);
    }

    @Override
    public List<Voucher> voucherLists() {
        return storage.values().stream()
                .toList();
    }

    //constructor
    private void validateAmountOrPercentRange(long amountOrPercent) {
        if (amountOrPercent < 0 || amountOrPercent > 100) {
            throw new InvalidRangeException("PercentDiscountPolicy는 0~100 사이의 값을 가져야 합니다.");
        }
    }
}
