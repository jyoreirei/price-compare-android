package com.example.pricecompare.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PriceCalculator {
    private static final BigDecimal TEN = new BigDecimal("10");
    private static final BigDecimal THOUSAND = new BigDecimal("1000");

    private PriceCalculator() {}

    public enum MeasureType { WEIGHT, VOLUME, COUNT }
    public enum DiscountType { NONE, MINUS, RATE, FINAL }

    public static final class ProductInput {
        public final String name;
        public final BigDecimal markedPrice;
        public final MeasureType measureType;
        public final BigDecimal singleSize;
        public final String unit;
        public final int packCount;
        public final DiscountType discountType;
        public final BigDecimal discountValue;

        public ProductInput(
                String name,
                BigDecimal markedPrice,
                MeasureType measureType,
                BigDecimal singleSize,
                String unit,
                int packCount,
                DiscountType discountType,
                BigDecimal discountValue) {
            this.name = name;
            this.markedPrice = markedPrice;
            this.measureType = measureType;
            this.singleSize = singleSize;
            this.unit = unit;
            this.packCount = packCount;
            this.discountType = discountType;
            this.discountValue = discountValue;
        }
    }

    public static final class RankedProduct {
        public final ProductInput input;
        public final BigDecimal paidPrice;
        public final BigDecimal totalBaseSize;
        public final BigDecimal unitPrice;

        private RankedProduct(ProductInput input, BigDecimal paidPrice,
                              BigDecimal totalBaseSize, BigDecimal unitPrice) {
            this.input = input;
            this.paidPrice = paidPrice;
            this.totalBaseSize = totalBaseSize;
            this.unitPrice = unitPrice;
        }
    }

    public static final class Comparison {
        public final List<RankedProduct> ranking;
        public final BigDecimal savingsAgainstSecond;

        private Comparison(List<RankedProduct> ranking, BigDecimal savingsAgainstSecond) {
            this.ranking = ranking;
            this.savingsAgainstSecond = savingsAgainstSecond;
        }
    }

    public static Comparison compare(List<ProductInput> inputs) {
        if (inputs == null || inputs.size() < 2 || inputs.size() > 5) {
            throw new IllegalArgumentException("商品数量必须为 2～5 个");
        }
        MeasureType type = inputs.get(0).measureType;
        String countUnit = inputs.get(0).unit;
        List<RankedProduct> ranking = new ArrayList<>();
        for (ProductInput input : inputs) {
            validate(input);
            if (input.measureType != type) {
                throw new IllegalArgumentException("只能比较同一计量类型的商品");
            }
            if (type == MeasureType.COUNT && !countUnit.equals(input.unit)) {
                throw new IllegalArgumentException("数量类商品必须使用相同单位");
            }
            BigDecimal paid = paidPrice(input);
            BigDecimal total = toBaseSize(input).multiply(BigDecimal.valueOf(input.packCount));
            BigDecimal unitPrice = paid.divide(total, 12, RoundingMode.HALF_UP);
            ranking.add(new RankedProduct(input, money(paid), total, unitPrice));
        }
        ranking.sort(Comparator.comparing(product -> product.unitPrice));
        RankedProduct winner = ranking.get(0);
        RankedProduct second = ranking.get(1);
        BigDecimal savings = second.unitPrice.subtract(winner.unitPrice)
                .multiply(winner.totalBaseSize);
        return new Comparison(ranking, money(savings.max(BigDecimal.ZERO)));
    }

    public static BigDecimal displayUnitPrice(RankedProduct product) {
        BigDecimal factor = product.input.measureType == MeasureType.COUNT
                ? BigDecimal.ONE : THOUSAND;
        return money(product.unitPrice.multiply(factor));
    }

    public static String displayUnit(ProductInput input) {
        if (input.measureType == MeasureType.WEIGHT) return "千克";
        if (input.measureType == MeasureType.VOLUME) return "升";
        return input.unit;
    }

    private static void validate(ProductInput input) {
        if (input == null || input.measureType == null || input.discountType == null) {
            throw new IllegalArgumentException("商品信息不完整");
        }
        if (input.markedPrice == null || input.markedPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(input.name + "：商品标价必须大于 0");
        }
        if (input.singleSize == null || input.singleSize.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(input.name + "：单件规格必须大于 0");
        }
        if (input.packCount < 1) {
            throw new IllegalArgumentException(input.name + "：组合数量必须大于 0");
        }
    }

    private static BigDecimal paidPrice(ProductInput input) {
        BigDecimal value = input.discountValue;
        switch (input.discountType) {
            case NONE:
                return input.markedPrice;
            case MINUS:
                requireValue(input, value);
                if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(input.markedPrice) >= 0) {
                    throw new IllegalArgumentException(input.name + "：立减金额必须小于商品标价");
                }
                return input.markedPrice.subtract(value);
            case RATE:
                requireValue(input, value);
                if (value.compareTo(BigDecimal.ZERO) <= 0 || value.compareTo(TEN) > 0) {
                    throw new IllegalArgumentException(input.name + "：折扣必须大于 0 且不超过 10 折");
                }
                return input.markedPrice.multiply(value).divide(TEN, 12, RoundingMode.HALF_UP);
            case FINAL:
                requireValue(input, value);
                if (value.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(input.name + "：实付金额必须大于 0");
                }
                return value;
            default:
                throw new IllegalArgumentException("不支持的优惠方式");
        }
    }

    private static void requireValue(ProductInput input, BigDecimal value) {
        if (value == null) throw new IllegalArgumentException(input.name + "：请输入优惠值");
    }

    private static BigDecimal toBaseSize(ProductInput input) {
        if (input.measureType == MeasureType.WEIGHT && "千克".equals(input.unit)) {
            return input.singleSize.multiply(THOUSAND);
        }
        if (input.measureType == MeasureType.VOLUME && "升".equals(input.unit)) {
            return input.singleSize.multiply(THOUSAND);
        }
        return input.singleSize;
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
