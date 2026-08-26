package com.example.pricecompare.domain;

import java.math.BigDecimal;
import java.util.Arrays;

public final class PriceCalculatorTest {
    public static void main(String[] args) {
        comparesDifferentWeightUnitsAndDiscounts();
        comparesCombinationPacks();
        rejectsMixedMeasureTypes();
        rejectsInvalidDiscount();
        System.out.println("PriceCalculator: 4 tests passed");
    }

    private static void comparesDifferentWeightUnitsAndDiscounts() {
        PriceCalculator.ProductInput a = product("商品 A", "20", "500", "克", 1,
                PriceCalculator.MeasureType.WEIGHT, PriceCalculator.DiscountType.MINUS, "2");
        PriceCalculator.ProductInput b = product("商品 B", "35", "1", "千克", 1,
                PriceCalculator.MeasureType.WEIGHT, PriceCalculator.DiscountType.RATE, "9");
        PriceCalculator.Comparison result = PriceCalculator.compare(Arrays.asList(a, b));
        assertEquals("商品 B", result.ranking.get(0).input.name);
        assertEquals("31.50", PriceCalculator.displayUnitPrice(result.ranking.get(0)).toPlainString());
        assertEquals("4.50", result.savingsAgainstSecond.toPlainString());
    }

    private static void comparesCombinationPacks() {
        PriceCalculator.ProductInput a = product("12 瓶", "48", "500", "毫升", 12,
                PriceCalculator.MeasureType.VOLUME, PriceCalculator.DiscountType.NONE, null);
        PriceCalculator.ProductInput b = product("8 瓶", "42", "750", "毫升", 8,
                PriceCalculator.MeasureType.VOLUME, PriceCalculator.DiscountType.NONE, null);
        PriceCalculator.Comparison result = PriceCalculator.compare(Arrays.asList(a, b));
        assertEquals("8 瓶", result.ranking.get(0).input.name);
        assertEquals("7.00", PriceCalculator.displayUnitPrice(result.ranking.get(0)).toPlainString());
    }

    private static void rejectsMixedMeasureTypes() {
        PriceCalculator.ProductInput weight = product("重量", "10", "500", "克", 1,
                PriceCalculator.MeasureType.WEIGHT, PriceCalculator.DiscountType.NONE, null);
        PriceCalculator.ProductInput volume = product("容量", "10", "500", "毫升", 1,
                PriceCalculator.MeasureType.VOLUME, PriceCalculator.DiscountType.NONE, null);
        expectFailure(() -> PriceCalculator.compare(Arrays.asList(weight, volume)));
    }

    private static void rejectsInvalidDiscount() {
        PriceCalculator.ProductInput bad = product("错误折扣", "10", "1", "个", 1,
                PriceCalculator.MeasureType.COUNT, PriceCalculator.DiscountType.RATE, "11");
        PriceCalculator.ProductInput ok = product("正常", "10", "1", "个", 1,
                PriceCalculator.MeasureType.COUNT, PriceCalculator.DiscountType.NONE, null);
        expectFailure(() -> PriceCalculator.compare(Arrays.asList(bad, ok)));
    }

    private static PriceCalculator.ProductInput product(String name, String price, String size,
            String unit, int packs, PriceCalculator.MeasureType type,
            PriceCalculator.DiscountType discount, String discountValue) {
        return new PriceCalculator.ProductInput(name, new BigDecimal(price), type,
                new BigDecimal(size), unit, packs, discount,
                discountValue == null ? null : new BigDecimal(discountValue));
    }

    private static void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) throw new AssertionError("expected=" + expected + ", actual=" + actual);
    }

    private static void expectFailure(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Expected failure");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }
}
