package com.hhjs.psi.sales.importing.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TextOrderParser {

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final Pattern MOBILE_PATTERN = Pattern.compile("1\\d{10}");
    private static final Pattern PRICE_PATTERN = Pattern.compile("(?:单价|价格|价钱|¥|￥)\\s*[:：-]?\\s*([0-9]+(?:\\.[0-9]{1,2})?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern KEYWORD_QUANTITY_PATTERN = Pattern.compile("(?:数量|qty|件数|份数)\\s*[:：-]?\\s*([0-9]+(?:\\.[0-9]+)?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SUFFIX_QUANTITY_PATTERN = Pattern.compile("(?:[xX×*]|每样|各)\\s*([0-9]+(?:\\.[0-9]+)?)");
    private static final Pattern PACKAGE_SPEC_PATTERN = Pattern.compile("([0-9]+)\\s*(?:个装|个|枚装|只装)");
    private static final Pattern CHINESE_QUANTITY_PATTERN = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*(?:件|盒|袋|份|箱|包|组|套|条|只|斤|kg|KG)");
    private static final Pattern SKU_PATTERN = Pattern.compile("(?:sku|规格编码|编码)\\s*[:：-]?\\s*([A-Za-z0-9_\\-]+)", Pattern.CASE_INSENSITIVE);
    private static final List<String> ORDER_KEYWORDS = List.of("订单号", "单号", "订单", "order", "编号");
    private static final List<String> CUSTOMER_KEYWORDS = List.of("客户", "收件人", "联系人", "姓名", "客户名");
    private static final List<String> PHONE_KEYWORDS = List.of("电话", "手机", "联系方式", "联系电话", "收件人电话");
    private static final List<String> ADDRESS_KEYWORDS = List.of("地址", "收货地址", "收件地址", "详细地址");
    private static final List<String> BUYER_MESSAGE_KEYWORDS = List.of("买家留言", "客户备注", "顾客备注");
    private static final List<String> SELLER_REMARK_KEYWORDS = List.of("卖家备注", "客服备注", "内部备注", "备注");
    private static final List<String> ITEM_KEYWORDS = List.of("商品", "明细", "品名", "商品名", "货品", "规格");

    List<ParsedOrder> parse(String rawText) {
        List<String> normalizedLines = Arrays.stream(rawText.split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
        if (normalizedLines.isEmpty()) {
            return List.of();
        }

        List<String> blocks = new ArrayList<>();
        StringBuilder currentBlock = new StringBuilder();

        for (String line : normalizedLines) {
            boolean explicitBreak = currentBlock.length() > 0 && isOrderHeaderLine(line) && !looksLikeItemLine(line);
            if (explicitBreak) {
                blocks.add(currentBlock.toString());
                currentBlock = new StringBuilder();
            }
            if (!currentBlock.isEmpty()) {
                currentBlock.append('\n');
            }
            currentBlock.append(line);
        }
        if (!currentBlock.isEmpty()) {
            blocks.add(currentBlock.toString());
        }

        List<ParsedOrder> orders = new ArrayList<>();
        int index = 1;
        for (String block : blocks) {
            if (block != null && !block.isBlank()) {
                orders.add(parseBlock(index++, block));
            }
        }
        return orders;
    }

    ParsedOrder parseBlock(int index, String block) {
        String externalOrderNo = null;
        String customerName = null;
        String customerPhone = null;
        String customerAddress = null;
        String buyerMessage = null;
        String sellerRemark = null;
        List<ParsedItem> items = new ArrayList<>();

        List<String> lines = Arrays.stream(block.split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();

        ReceiverInfo looseReceiver = parseLooseReceiver(lines);
        boolean consumedLooseReceiver = looseReceiver != null;
        if (looseReceiver != null) {
            customerName = firstNonBlank(looseReceiver.name(), customerName);
            customerPhone = firstNonBlank(looseReceiver.phone(), customerPhone);
            customerAddress = firstNonBlank(looseReceiver.address(), customerAddress);
        }

        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            String line = lines.get(lineIndex);
            String value = readValue(line);
            if (consumedLooseReceiver && lineIndex <= 2) {
                continue;
            }
            if (lineIndex == 0 && isWechatReceiverLine(line)) {
                ReceiverInfo receiverInfo = parseReceiverLine(line);
                customerName = firstNonBlank(receiverInfo.name(), customerName);
                customerPhone = firstNonBlank(receiverInfo.phone(), customerPhone);
                customerAddress = firstNonBlank(receiverInfo.address(), customerAddress);
                continue;
            }
            if (matchesAnyKey(line, ORDER_KEYWORDS)) {
                externalOrderNo = firstNonBlank(value, extractOrderNo(line));
                continue;
            }
            if (matchesAnyKey(line, CUSTOMER_KEYWORDS)) {
                customerName = firstNonBlank(value, customerName);
                continue;
            }
            if (matchesAnyKey(line, PHONE_KEYWORDS)) {
                customerPhone = firstNonBlank(extractPhone(value), extractPhone(line), customerPhone);
                continue;
            }
            if (matchesAnyKey(line, ADDRESS_KEYWORDS)) {
                customerAddress = firstNonBlank(value, customerAddress);
                continue;
            }
            if (matchesAnyKey(line, BUYER_MESSAGE_KEYWORDS)) {
                buyerMessage = appendMessage(buyerMessage, value);
                continue;
            }
            if (matchesAnyKey(line, SELLER_REMARK_KEYWORDS)) {
                sellerRemark = appendMessage(sellerRemark, value);
                continue;
            }
            if (matchesAnyKey(line, ITEM_KEYWORDS) || looksLikeItemLine(line)) {
                items.addAll(parseItemCandidates(value.isBlank() ? line : value));
                continue;
            }
            if (customerPhone == null) {
                customerPhone = extractPhone(line);
            }
            if (customerAddress == null && looksLikeAddress(line)) {
                customerAddress = line;
                continue;
            }
            if (customerName == null && looksLikeNameLine(line)) {
                customerName = line;
                continue;
            }
            if (items.isEmpty() && line.length() <= 80) {
                items.addAll(parseItemCandidates(line));
            }
        }

        if (externalOrderNo == null || externalOrderNo.isBlank()) {
            externalOrderNo = "TEXT-" + LocalDateTime.now().format(ORDER_NO_FORMATTER) + "-" + index;
        }
        if (items.isEmpty() && !lines.isEmpty()) {
            items.add(parseItemLine(lines.getFirst()));
        }
        return new ParsedOrder(
                externalOrderNo,
                normalizeOptional(customerName),
                normalizeOptional(extractPhone(customerPhone)),
                normalizeOptional(customerAddress),
                normalizeOptional(buyerMessage),
                normalizeOptional(sellerRemark),
                items
        );
    }

    List<ParsedItem> parseItemCandidates(String line) {
        List<ParsedItem> items = new ArrayList<>();
        for (String part : splitItemSegments(line)) {
            String normalized = normalizeItemPrefix(part);
            if (!normalized.isBlank()) {
                items.add(parseItemLine(normalized));
            }
        }
        return items.isEmpty() ? List.of(parseItemLine(line)) : items;
    }

    ParsedItem parseItemLine(String line) {
        String normalized = normalizeItemPrefix(line);
        String skuCode = extractSkuCode(normalized);
        BigDecimal quantity = extractQuantity(normalized);
        BigDecimal unitPrice = extractPrice(normalized);
        String productName = removeItemMeta(normalized);
        String specName = firstNonBlank(extractPackageSpec(normalized), extractSpecName(productName));
        if (specName != null && specName.matches("\\d+个装?")) {
            quantity = BigDecimal.ONE;
        }
        productName = removePackageSpec(productName);
        productName = normalizeOptional(productName);
        specName = normalizeOptional(normalizePackageSpec(specName));
        if (productName == null) {
            productName = normalized;
        }
        return new ParsedItem(productName, specName, skuCode, quantity, unitPrice);
    }

    private List<String> splitItemSegments(String line) {
        String normalized = line.replace('；', ';').replace('，', ',');
        if (normalized.contains(";") || normalized.contains(",")) {
            return Arrays.stream(normalized.split("[;,]"))
                    .map(String::trim)
                    .filter(part -> !part.isBlank())
                    .toList();
        }
        return List.of(normalized);
    }

    private String normalizeItemPrefix(String line) {
        String normalized = line.trim();
        normalized = normalized.replaceFirst("^(商品|明细|品名|商品名|货品|规格)\\s*[:：-]?\\s*", "");
        normalized = normalized.replaceFirst("^[-•·]\\s*", "");
        return normalized.trim();
    }

    private String removeItemMeta(String line) {
        String result = line;
        result = PRICE_PATTERN.matcher(result).replaceAll("");
        result = KEYWORD_QUANTITY_PATTERN.matcher(result).replaceAll("");
        result = SUFFIX_QUANTITY_PATTERN.matcher(result).replaceAll("");
        result = Pattern.compile("(?:sku|规格编码|编码)\\s*[:：-]?\\s*[A-Za-z0-9_\\-]+", Pattern.CASE_INSENSITIVE).matcher(result).replaceAll("");
        result = Pattern.compile("(?:共|合计)\\s*[0-9]+(?:\\.[0-9]+)?\\s*(?:件|盒|袋|份|箱|包|组|套|条|只)").matcher(result).replaceAll("");
        return result.replaceAll("\\(\\s*\\)", "").replaceAll("（\\s*）", "").replaceAll("\\s{2,}", " ").trim();
    }

    private String extractPackageSpec(String line) {
        Matcher matcher = PACKAGE_SPEC_PATTERN.matcher(line);
        if (matcher.find()) {
            return matcher.group(1) + "个装";
        }
        return null;
    }

    private String removePackageSpec(String value) {
        if (value == null) {
            return null;
        }
        return PACKAGE_SPEC_PATTERN.matcher(value).replaceAll("").replaceAll("\\s{2,}", " ").trim();
    }

    private String normalizePackageSpec(String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = PACKAGE_SPEC_PATTERN.matcher(value);
        if (matcher.find()) {
            return matcher.group(1) + "个装";
        }
        return value;
    }

    private String extractSpecName(String productName) {
        if (productName == null) {
            return null;
        }
        String bracketSpec = extractBetween(productName, '（', '）');
        if (bracketSpec != null) {
            return bracketSpec;
        }
        bracketSpec = extractBetween(productName, '(', ')');
        if (bracketSpec != null) {
            return bracketSpec;
        }
        Matcher matcher = Pattern.compile("([0-9]+\\s*(?:g|G|克|kg|KG|斤|个装|枚装|只装|袋装|盒装)(?:\\s*[/每]?\\s*(?:个|只|袋|盒|箱|包))?)").matcher(productName);
        if (matcher.find()) {
            return matcher.group(1).replaceAll("\\s+", "");
        }
        return null;
    }

    private String extractBetween(String value, char leftChar, char rightChar) {
        int left = value.indexOf(leftChar);
        int right = value.indexOf(rightChar);
        if (left >= 0 && right > left) {
            return value.substring(left + 1, right).trim();
        }
        return null;
    }

    private BigDecimal extractQuantity(String line) {
        Matcher keywordMatcher = KEYWORD_QUANTITY_PATTERN.matcher(line);
        if (keywordMatcher.find()) {
            return new BigDecimal(keywordMatcher.group(1));
        }
        Matcher suffixMatcher = SUFFIX_QUANTITY_PATTERN.matcher(line);
        if (suffixMatcher.find()) {
            return new BigDecimal(suffixMatcher.group(1));
        }
        Matcher chineseMatcher = CHINESE_QUANTITY_PATTERN.matcher(line);
        BigDecimal lastValue = null;
        while (chineseMatcher.find()) {
            lastValue = new BigDecimal(chineseMatcher.group(1));
        }
        return lastValue == null ? BigDecimal.ONE : lastValue;
    }

    private BigDecimal extractPrice(String line) {
        Matcher matcher = PRICE_PATTERN.matcher(line);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        return BigDecimal.ZERO;
    }

    private String extractSkuCode(String line) {
        Matcher matcher = SKU_PATTERN.matcher(line);
        return matcher.find() ? matcher.group(1) : null;
    }

    private boolean matchesAnyKey(String line, List<String> keywords) {
        String normalized = line.toLowerCase();
        return keywords.stream().anyMatch(keyword -> normalized.startsWith(keyword.toLowerCase()));
    }

    private String readValue(String line) {
        int index = Math.max(line.indexOf('：'), line.indexOf(':'));
        return index >= 0 ? line.substring(index + 1).trim() : "";
    }

    private String extractOrderNo(String line) {
        String value = readValue(line);
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        return line.replaceAll("^(订单号|单号|订单|order|编号)\\s*[:：-]?\\s*", "").trim();
    }

    private String extractPhone(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        Matcher matcher = MOBILE_PATTERN.matcher(text.replaceAll("\\s+", ""));
        return matcher.find() ? matcher.group() : null;
    }

    private ReceiverInfo parseLooseReceiver(List<String> lines) {
        if (lines.size() < 3) {
            return null;
        }
        String nameLine = lines.get(0);
        String phoneLine = lines.get(1);
        String addressLine = lines.get(2);
        String phone = extractPhone(phoneLine);
        if (phone == null || !looksLikeNameLine(nameLine) || !looksLikeAddress(addressLine)) {
            return null;
        }
        return new ReceiverInfo(normalizeOptional(nameLine), phone, normalizeOptional(addressLine));
    }

    private boolean isOrderHeaderLine(String line) {
        return matchesAnyKey(line, ORDER_KEYWORDS) || isWechatReceiverLine(line);
    }

    private boolean isWechatReceiverLine(String line) {
        return extractPhone(line) != null && looksLikeAddress(line) && line.length() <= 220;
    }

    private ReceiverInfo parseReceiverLine(String line) {
        String phone = extractPhone(line);
        String name = null;
        String address = line;
        if (phone != null) {
            int phoneIndex = line.indexOf(phone);
            if (phoneIndex >= 0) {
                String beforePhone = line.substring(0, phoneIndex).replaceAll("[，,、;；:：-]+$", "").trim();
                name = normalizeReceiverName(beforePhone);
                address = line.substring(phoneIndex + phone.length()).replaceFirst("^[-—,，、;；\\s]+", "").trim();
            }
        }
        if ((address == null || address.isBlank()) && phone != null) {
            address = line.replace(phone, "").trim();
        }
        return new ReceiverInfo(normalizeOptional(name), phone, normalizeOptional(address));
    }

    private String normalizeReceiverName(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = simplifyReceiverName(value);
        if (normalized.contains(",")) {
            normalized = normalized.substring(0, normalized.indexOf(',')).trim();
        }
        if (normalized.contains("，")) {
            normalized = normalized.substring(0, normalized.indexOf('，')).trim();
        }
        if (normalized.length() > 20) {
            return null;
        }
        return normalized;
    }

    private String simplifyReceiverName(String value) {
        String normalized = value.trim();
        normalized = normalized.replaceAll("[\\[（(【]\\s*\\d{2,8}\\s*[\\]）)】]$", "").trim();
        normalized = normalized.replaceAll("[-—_]?\\s*\\d{2,8}$", "").trim();
        return normalized;
    }

    private boolean looksLikeAddress(String line) {
        return line.contains("省") || line.contains("市") || line.contains("区") || line.contains("县") || line.contains("镇") || line.contains("村") || line.contains("路") || line.contains("号") || line.contains("组");
    }

    private boolean looksLikeNameLine(String line) {
        String normalized = simplifyReceiverName(line);
        return normalized.length() <= 12 && !normalized.matches(".*\\d.*") && !looksLikeItemLine(normalized);
    }

    private boolean looksLikeItemLine(String line) {
        return line.contains("x") || line.contains("×") || line.contains("*") || line.contains("¥") || line.contains("￥") || line.contains("数量") || line.contains("单价");
    }

    private String appendMessage(String current, String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            return current;
        }
        if (current == null || current.isBlank()) {
            return normalized;
        }
        return current + "；" + normalized;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            String normalized = normalizeOptional(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    record ParsedOrder(String externalOrderNo, String customerName, String customerPhone, String customerAddress, String buyerMessage, String sellerRemark, List<ParsedItem> items) {}
    record ParsedItem(String productName, String specName, String skuCode, BigDecimal quantity, BigDecimal unitPrice) {}
    record ReceiverInfo(String name, String phone, String address) {}
}
