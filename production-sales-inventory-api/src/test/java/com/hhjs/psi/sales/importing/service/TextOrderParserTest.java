package com.hhjs.psi.sales.importing.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextOrderParserTest {

    private final TextOrderParser parser = new TextOrderParser();

    @Test
    void shouldParseStandardTextOrderBlock() {
        String text = """
                订单号：WX001
                客户：张三
                电话：13800000000
                地址：上海市浦东新区张江路88号
                商品：蛋黄鲜肉粽（188g）*10 ¥12.5
                买家留言：尽快发货
                """;

        List<TextOrderParser.ParsedOrder> orders = parser.parse(text);
        assertEquals(1, orders.size());

        TextOrderParser.ParsedOrder order = orders.getFirst();
        assertEquals("WX001", order.externalOrderNo());
        assertEquals("张三", order.customerName());
        assertEquals("13800000000", order.customerPhone());
        assertEquals("上海市浦东新区张江路88号", order.customerAddress());
        assertEquals("尽快发货", order.buyerMessage());
        assertEquals(1, order.items().size());

        TextOrderParser.ParsedItem item = order.items().getFirst();
        assertEquals("蛋黄鲜肉粽（188g）", item.productName());
        assertEquals("188g", item.specName());
        assertEquals(new BigDecimal("10"), item.quantity());
        assertEquals(new BigDecimal("12.5"), item.unitPrice());
    }

    @Test
    void shouldParseMultipleItemsSeparatedByCommaAndSemicolon() {
        String text = "商品：蛋黄鲜肉粽x10，豆沙粽×5；蜜枣粽*3";

        List<TextOrderParser.ParsedItem> items = parser.parseItemCandidates(text);
        assertEquals(3, items.size());
        assertEquals("蛋黄鲜肉粽", items.get(0).productName());
        assertEquals(new BigDecimal("10"), items.get(0).quantity());
        assertEquals("豆沙粽", items.get(1).productName());
        assertEquals(new BigDecimal("5"), items.get(1).quantity());
        assertEquals("蜜枣粽", items.get(2).productName());
        assertEquals(new BigDecimal("3"), items.get(2).quantity());
    }

    @Test
    void shouldExtractSpecSkuQuantityAndPriceFromComplexItemLine() {
        TextOrderParser.ParsedItem item = parser.parseItemLine("品名：蛋黄鲜肉粽(188g/袋) sku:ZONG001 数量: 12 单价: 15.80");

        assertEquals("蛋黄鲜肉粽(188g/袋)", item.productName());
        assertEquals("188g/袋", item.specName());
        assertEquals("ZONG001", item.skuCode());
        assertEquals(new BigDecimal("12"), item.quantity());
        assertEquals(new BigDecimal("15.80"), item.unitPrice());
    }

    @Test
    void shouldGenerateFallbackOrderNoAndInferPhoneAddress() {
        String text = """
                李四
                13911112222
                浙江省杭州市西湖区文三路100号
                豆沙粽 5个
                """;

        TextOrderParser.ParsedOrder order = parser.parse(text).getFirst();
        assertTrue(order.externalOrderNo().startsWith("TEXT-"));
        assertEquals("李四", order.customerName());
        assertEquals("13911112222", order.customerPhone());
        assertEquals("浙江省杭州市西湖区文三路100号", order.customerAddress());
        assertEquals(1, order.items().size());
        assertEquals(new BigDecimal("5"), order.items().getFirst().quantity());
    }

    @Test
    void shouldParseReceiverWithBracketSuffixAndPackageSpec() {
        String text = """
                郑美灵[9606]
                17284238509
                广东省 东莞市 石排镇 向西村石崇工业园旭柏彩印厂[9606]

                蛋黄鲜肉粽*4个
                """;

        TextOrderParser.ParsedOrder order = parser.parse(text).getFirst();
        assertEquals("郑美灵", order.customerName());
        assertEquals("17284238509", order.customerPhone());
        assertEquals("广东省 东莞市 石排镇 向西村石崇工业园旭柏彩印厂[9606]", order.customerAddress());
        assertEquals(1, order.items().size());
        assertEquals("蛋黄鲜肉粽", order.items().getFirst().productName());
        assertEquals("4个装", order.items().getFirst().specName());
        assertEquals(BigDecimal.ONE, order.items().getFirst().quantity());
    }

    @Test
    void shouldKeepNullSpecWhenItemHasNoSpec() {
        TextOrderParser.ParsedItem item = parser.parseItemLine("豆沙粽 x8");
        assertEquals("豆沙粽", item.productName());
        assertNull(item.specName());
        assertEquals(new BigDecimal("8"), item.quantity());
        assertEquals(BigDecimal.ZERO, item.unitPrice());
    }

    @Test
    void shouldParseMultipleBlocksIntoOrders() {
        String text = """
                订单号：A001
                客户：张三
                商品：蛋黄鲜肉粽x2

                单号：A002
                收件人：李四
                手机：13812345678
                商品：豆沙粽x3
                """;

        List<TextOrderParser.ParsedOrder> orders = parser.parse(text);
        assertEquals(2, orders.size());
        assertEquals("A001", orders.get(0).externalOrderNo());
        assertEquals("A002", orders.get(1).externalOrderNo());
        assertEquals("13812345678", orders.get(1).customerPhone());
        assertNotNull(orders.get(0).items());
        assertNotNull(orders.get(1).items());
    }
}
