package org.prgrms.vouchermanagement.webController;

import org.prgrms.vouchermanagement.dto.VoucherCreateInfo;
import org.prgrms.vouchermanagement.exception.InvalidInputException;
import org.prgrms.vouchermanagement.voucher.domain.Voucher;
import org.prgrms.vouchermanagement.voucher.policy.PolicyStatus;
import org.prgrms.vouchermanagement.voucher.service.VoucherService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/voucher")
public class VoucherApiController {

    private final VoucherService voucherService;

    public VoucherApiController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping("/list")
    public List<Voucher> voucherList() {
        return voucherService.voucherLists();
    }

    @PostMapping
    public Voucher createVoucher(@RequestBody VoucherCreateInfo voucherCreateInfo) {
        PolicyStatus policy = validateAndConvertPolicy(voucherCreateInfo.policy().toString());
        long amountOrPercent = validateAndConvertAmountOrPercent(String.valueOf(voucherCreateInfo.amountOrPercent()));

        return voucherService.createVoucher(policy, amountOrPercent);
    }

    @GetMapping
    public List<Voucher> findVouchers(
            @RequestParam(name = "voucherId", required = false) String voucherId,
            @RequestParam(name = "policy", required = false) String policy) {

        List<Voucher> vouchers = new ArrayList<>();

        if (voucherId != null && policy != null) {
            // voucherId와 policy 모두 제공된 경우
            UUID getVoucherId = UUID.fromString(voucherId);
            Voucher findVoucher = voucherService.findVoucher(getVoucherId);
            vouchers.add(findVoucher);
        } else if (voucherId != null) {
            // voucherId만 제공된 경우
            UUID getVoucherId = UUID.fromString(voucherId);
            Voucher findVoucher = voucherService.findVoucher(getVoucherId);
            vouchers.add(findVoucher);
        } else if (policy != null) {
            // policy만 제공된 경우
            PolicyStatus getPolicy = validateAndConvertPolicy(policy);
            vouchers = voucherService.findVouchersByPolicy(getPolicy);
        }

        return vouchers;
    }

    @DeleteMapping("{voucherId}")
    public Voucher deleteVoucher(@PathVariable UUID voucherId) {
        return voucherService.deleteVoucher(voucherId);
    }

    private PolicyStatus validateAndConvertPolicy(String inputPolicy) {
        try {
            return PolicyStatus.valueOf(inputPolicy.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("잘못된 policy 입력입니다.");
        }
    }

    private long validateAndConvertAmountOrPercent(String inputAmountOrPercent) {
        try {
            return Long.parseLong(inputAmountOrPercent);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("잘못된 amountOrPercent 입력입니다.");
        }
    }

}