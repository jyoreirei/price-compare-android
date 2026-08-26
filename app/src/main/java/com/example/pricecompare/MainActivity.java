package com.example.pricecompare;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pricecompare.domain.PriceCalculator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final int PAPER = Color.WHITE;
    private static final int SURFACE = Color.WHITE;
    private static final int INK = Color.rgb(17, 17, 17);
    private static final int MUTED = Color.rgb(102, 102, 102);
    private static final int GREEN = Color.rgb(17, 17, 17);
    private static final int GREEN_DARK = Color.BLACK;
    private static final int GREEN_SOFT = Color.rgb(244, 244, 244);
    private static final int LINE = Color.rgb(215, 215, 215);
    private static final int DANGER = Color.rgb(17, 17, 17);

    private final List<ProductForm> forms = new ArrayList<>();
    private LinearLayout cardsContainer;
    private LinearLayout resultContainer;
    private TextView countText;
    private Button addButton;
    private ScrollView scrollView;
    private boolean binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(GREEN_DARK);
        window.setNavigationBarColor(PAPER);
        setContentView(buildScreen());
        addProduct();
        addProduct();
    }

    private View buildScreen() {
        scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(PAPER);

        LinearLayout root = column();
        root.setPadding(dp(16), dp(16), dp(16), dp(40));
        scrollView.addView(root, matchWrap());

        LinearLayout brand = row();
        brand.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo = text("↗", 25, Color.WHITE, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(roundRect(INK, 12, 0, Color.TRANSPARENT));
        brand.addView(logo, new LinearLayout.LayoutParams(dp(44), dp(44)));
        LinearLayout brandCopy = column();
        brandCopy.setPadding(dp(12), 0, 0, 0);
        brandCopy.addView(text("比价助手", 22, INK, true));
        brandCopy.addView(text("离线单位价格比较", 12, MUTED, false));
        brand.addView(brandCopy, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        root.addView(brand, matchWrap());

        resultContainer = column();
        resultContainer.setVisibility(View.GONE);
        LinearLayout.LayoutParams resultParams = matchWrap();
        resultParams.topMargin = dp(16);
        root.addView(resultContainer, resultParams);

        LinearLayout panel = column();
        panel.setPadding(dp(12), dp(16), dp(12), dp(12));
        panel.setBackground(roundRect(SURFACE, 18, 1, LINE));
        LinearLayout header = row();
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(text("商品信息", 18, INK, true), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        countText = text("2 / 5 件", 12, MUTED, false);
        header.addView(countText);
        panel.addView(header);

        cardsContainer = column();
        LinearLayout.LayoutParams cardsParams = matchWrap();
        cardsParams.topMargin = dp(10);
        panel.addView(cardsContainer, cardsParams);

        addButton = button("＋ 添加商品", false);
        addButton.setOnClickListener(v -> addProduct());
        LinearLayout.LayoutParams addParams = matchWrap();
        addParams.topMargin = dp(6);
        panel.addView(addButton, addParams);

        Button compareButton = button("开始比较", true);
        compareButton.setOnClickListener(v -> compareProducts());
        LinearLayout.LayoutParams compareParams = matchWrap();
        compareParams.topMargin = dp(10);
        panel.addView(compareButton, compareParams);

        LinearLayout.LayoutParams panelParams = matchWrap();
        panelParams.topMargin = dp(16);
        root.addView(panel, panelParams);

        return scrollView;
    }

    private void addProduct() {
        if (forms.size() >= 5) return;
        ProductForm form = new ProductForm(forms.size());
        forms.add(form);
        cardsContainer.addView(form.card, matchWrapWithBottom(10));
        refreshForms();
        invalidateResult();
    }

    private void removeProduct(ProductForm form) {
        if (forms.size() <= 2) {
            toast("至少需要保留两个商品");
            return;
        }
        forms.remove(form);
        cardsContainer.removeView(form.card);
        refreshForms();
        invalidateResult();
    }

    private void refreshForms() {
        for (int i = 0; i < forms.size(); i++) forms.get(i).setIndex(i);
        countText.setText(String.format(Locale.CHINA, "%d / 5 件", forms.size()));
        addButton.setEnabled(forms.size() < 5);
        addButton.setAlpha(forms.size() < 5 ? 1f : .45f);
    }

    private void compareProducts() {
        hideKeyboard();
        try {
            List<PriceCalculator.ProductInput> products = new ArrayList<>();
            for (int i = 0; i < forms.size(); i++) products.add(forms.get(i).toInput(i));
            PriceCalculator.Comparison comparison = PriceCalculator.compare(products);
            renderResult(comparison);
        } catch (IllegalArgumentException error) {
            invalidateResult();
            toast(error.getMessage());
        }
    }

    private void renderResult(PriceCalculator.Comparison comparison) {
        resultContainer.removeAllViews();
        resultContainer.setBackground(roundRect(INK, 18, 0, Color.TRANSPARENT));
        resultContainer.setPadding(dp(16), dp(19), dp(16), dp(16));
        PriceCalculator.RankedProduct winner = comparison.ranking.get(0);

        TextView kicker = text("比较结果", 12, Color.rgb(190, 190, 190), true);
        resultContainer.addView(kicker);
        TextView winnerName = text(winner.input.name, 25, Color.WHITE, true);
        LinearLayout.LayoutParams winnerNameParams = matchWrap();
        winnerNameParams.topMargin = dp(7);
        resultContainer.addView(winnerName, winnerNameParams);
        String summary = formatSize(winner.input) + " · 实付 ¥" + winner.paidPrice.toPlainString();
        resultContainer.addView(text(summary, 13, Color.rgb(208, 217, 210), false));

        TextView savings = text("省 ¥" + comparison.savingsAgainstSecond.toPlainString(), 30,
                Color.WHITE, true);
        LinearLayout.LayoutParams savingsParams = matchWrap();
        savingsParams.topMargin = dp(15);
        resultContainer.addView(savings, savingsParams);
        resultContainer.addView(text("按相同购买量与第二名比较", 11,
                Color.rgb(190, 190, 190), false));

        LinearLayout ranking = column();
        ranking.setPadding(0, dp(12), 0, 0);
        for (int i = 0; i < comparison.ranking.size(); i++) {
            PriceCalculator.RankedProduct product = comparison.ranking.get(i);
            LinearLayout row = row();
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(12), dp(11), dp(12), dp(11));
            row.setBackground(roundRect(i == 0 ? GREEN_SOFT : SURFACE, 12, 1, i == 0 ? GREEN_SOFT : LINE));

            TextView rank = text(String.valueOf(i + 1), 12, i == 0 ? Color.WHITE : INK, true);
            rank.setGravity(Gravity.CENTER);
            rank.setBackground(roundRect(i == 0 ? INK : Color.rgb(232, 232, 232), 9, 0, Color.TRANSPARENT));
            row.addView(rank, new LinearLayout.LayoutParams(dp(30), dp(30)));

            LinearLayout copy = column();
            copy.setPadding(dp(10), 0, dp(6), 0);
            copy.addView(text(product.input.name, 14, INK, true));
            copy.addView(text(formatSize(product.input) + " · 实付 ¥" + product.paidPrice, 11, MUTED, false));
            row.addView(copy, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            String unit = "¥" + PriceCalculator.displayUnitPrice(product) + " / "
                    + PriceCalculator.displayUnit(product.input);
            row.addView(text(unit, 12, INK, true));
            ranking.addView(row, matchWrapWithBottom(8));
        }
        resultContainer.addView(ranking);

        Button editAgain = button("修改后重新比较", false);
        editAgain.setOnClickListener(v -> {
            invalidateResult();
            scrollView.smoothScrollTo(0, cardsContainer.getTop());
        });
        resultContainer.addView(editAgain);
        resultContainer.setVisibility(View.VISIBLE);
        resultContainer.post(() -> scrollView.smoothScrollTo(0, resultContainer.getTop()));
    }

    private void invalidateResult() {
        if (resultContainer != null) {
            resultContainer.setVisibility(View.GONE);
            resultContainer.removeAllViews();
        }
    }

    private final class ProductForm {
        final LinearLayout card;
        final TextView title;
        final Button remove;
        final EditText name;
        final EditText price;
        final Spinner measureType;
        final EditText packs;
        final EditText size;
        final Spinner unit;
        final Spinner discountType;
        final LinearLayout discountWrap;
        final TextView discountLabel;
        final EditText discountValue;

        ProductForm(int index) {
            card = column();
            card.setPadding(dp(14), dp(14), dp(14), dp(14));
            card.setBackground(roundRect(Color.WHITE, 14, 1, LINE));

            LinearLayout cardHead = row();
            cardHead.setGravity(Gravity.CENTER_VERTICAL);
            title = text("商品 " + letter(index), 15, INK, true);
            cardHead.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            remove = button("删除", false);
            remove.setTextSize(12);
            remove.setTextColor(DANGER);
            remove.setMinHeight(0);
            remove.setMinimumHeight(0);
            remove.setPadding(dp(11), dp(5), dp(11), dp(5));
            remove.setOnClickListener(v -> removeProduct(this));
            cardHead.addView(remove, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(34)));
            card.addView(cardHead);

            name = input("例如：家庭装牛奶", false);
            card.addView(field("商品名称（选填）", name), matchWrapWithTop(12));

            price = input("0.00", true);
            measureType = spinner(new String[]{"重量", "容量", "数量"});
            card.addView(field("商品标价（元）", price), matchWrapWithTop(12));
            card.addView(field("计量类型", measureType), matchWrapWithTop(12));

            packs = input("1", true);
            packs.setInputType(InputType.TYPE_CLASS_NUMBER);
            packs.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
            packs.setText("1");
            size = input("例如：500", true);
            card.addView(field("组合数量", packs), matchWrapWithTop(12));
            card.addView(field("单件规格", size), matchWrapWithTop(12));

            unit = spinner(new String[]{"克", "千克"});
            discountType = spinner(new String[]{"无优惠", "立减金额", "折扣", "实付金额"});
            card.addView(field("规格单位", unit), matchWrapWithTop(12));
            card.addView(field("优惠方式", discountType), matchWrapWithTop(12));

            discountLabel = text("优惠值", 11, MUTED, true);
            discountValue = input("0.00", true);
            discountWrap = column();
            discountWrap.addView(discountLabel);
            discountWrap.addView(discountValue, matchWrapWithTop(7));
            discountWrap.setVisibility(View.GONE);
            card.addView(discountWrap, matchWrapWithTop(12));

            TextWatcher watcher = new SimpleWatcher();
            name.addTextChangedListener(watcher);
            price.addTextChangedListener(watcher);
            packs.addTextChangedListener(watcher);
            size.addTextChangedListener(watcher);
            discountValue.addTextChangedListener(watcher);

            measureType.setOnItemSelectedListener(new SelectionListener(() -> {
                if (!binding) {
                    updateUnitChoices();
                    invalidateResult();
                }
            }));
            unit.setOnItemSelectedListener(new SelectionListener(MainActivity.this::invalidateResult));
            discountType.setOnItemSelectedListener(new SelectionListener(() -> {
                updateDiscountField();
                invalidateResult();
            }));
        }

        void setIndex(int index) {
            title.setText("商品 " + letter(index));
            remove.setEnabled(forms.size() > 2);
            remove.setAlpha(forms.size() > 2 ? 1f : .38f);
        }

        void updateUnitChoices() {
            binding = true;
            String[] units = measureType.getSelectedItemPosition() == 0
                    ? new String[]{"克", "千克"}
                    : measureType.getSelectedItemPosition() == 1
                    ? new String[]{"毫升", "升"}
                    : new String[]{"个", "包", "盒"};
            unit.setAdapter(adapter(units));
            binding = false;
        }

        void updateDiscountField() {
            int selected = discountType.getSelectedItemPosition();
            discountWrap.setVisibility(selected == 0 ? View.GONE : View.VISIBLE);
            if (selected == 1) {
                discountLabel.setText("立减金额（元）");
                discountValue.setHint("0.00");
            } else if (selected == 2) {
                discountLabel.setText("折扣（例如 8.5）");
                discountValue.setHint("0.1～10 折");
            } else if (selected == 3) {
                discountLabel.setText("实付金额（元）");
                discountValue.setHint("0.00");
            }
        }

        PriceCalculator.ProductInput toInput(int index) {
            String fallback = "商品 " + letter(index);
            String productName = name.getText().toString().trim();
            if (productName.isEmpty()) productName = fallback;
            BigDecimal marked = decimal(price, fallback + "：请输入商品标价");
            BigDecimal single = decimal(size, fallback + "：请输入单件规格");
            int packCount;
            try {
                packCount = Integer.parseInt(packs.getText().toString().trim());
            } catch (NumberFormatException error) {
                throw new IllegalArgumentException(fallback + "：请输入正确的组合数量");
            }
            PriceCalculator.MeasureType type = PriceCalculator.MeasureType.values()[measureType.getSelectedItemPosition()];
            PriceCalculator.DiscountType discount = PriceCalculator.DiscountType.values()[discountType.getSelectedItemPosition()];
            BigDecimal discountNumber = discount == PriceCalculator.DiscountType.NONE
                    ? null : decimal(discountValue, fallback + "：请输入优惠值");
            return new PriceCalculator.ProductInput(productName, marked, type, single,
                    String.valueOf(unit.getSelectedItem()), packCount, discount, discountNumber);
        }
    }

    private final class SimpleWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { invalidateResult(); }
        @Override public void afterTextChanged(Editable s) {}
    }

    private static final class SelectionListener implements AdapterView.OnItemSelectedListener {
        private final Runnable action;
        SelectionListener(Runnable action) { this.action = action; }
        @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { action.run(); }
        @Override public void onNothingSelected(AdapterView<?> parent) {}
    }

    private BigDecimal decimal(EditText input, String message) {
        try {
            String value = input.getText().toString().trim();
            if (value.isEmpty()) throw new NumberFormatException();
            return new BigDecimal(value);
        } catch (NumberFormatException error) {
            input.requestFocus();
            throw new IllegalArgumentException(message);
        }
    }

    private String formatSize(PriceCalculator.ProductInput product) {
        String prefix = product.packCount > 1 ? product.packCount + " × " : "";
        return prefix + product.singleSize.stripTrailingZeros().toPlainString() + product.unit;
    }

    private LinearLayout field(String label, View input) {
        LinearLayout wrap = column();
        wrap.addView(text(label, 13, MUTED, true));
        wrap.addView(input, matchWrapWithTop(7));
        return wrap;
    }

    private EditText input(String hint, boolean numeric) {
        EditText edit = new EditText(this);
        edit.setTextSize(17);
        edit.setTextColor(INK);
        edit.setHintTextColor(Color.rgb(155, 160, 155));
        edit.setHint(hint);
        edit.setSingleLine(true);
        edit.setPadding(dp(14), 0, dp(14), 0);
        edit.setBackground(roundRect(SURFACE, 10, 1, INK));
        if (numeric) {
            edit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            edit.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        } else {
            edit.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        edit.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(58)));
        return edit;
    }

    private Spinner spinner(String[] values) {
        Spinner spinner = new Spinner(this, Spinner.MODE_DROPDOWN);
        spinner.setAdapter(adapter(values));
        spinner.setPadding(dp(11), 0, dp(11), 0);
        spinner.setBackground(roundRect(SURFACE, 10, 1, INK));
        spinner.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(58)));
        return spinner;
    }

    private ArrayAdapter<String> adapter(String[] values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, values) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(INK);
                view.setTextSize(17);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private Button button(String label, boolean primary) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(primary ? Color.WHITE : INK);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(12), 0, dp(12), 0);
        button.setBackground(roundRect(primary ? INK : Color.WHITE, 12, primary ? 0 : 1, INK));
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)));
        return button;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(sp);
        text.setTextColor(color);
        if (bold) text.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return text;
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout row() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        return layout;
    }

    private GradientDrawable roundRect(int color, int radiusDp, int strokeDp, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) drawable.setStroke(dp(strokeDp), strokeColor);
        return drawable;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams matchWrapWithTop(int marginDp) {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(marginDp);
        return params;
    }

    private LinearLayout.LayoutParams matchWrapWithBottom(int marginDp) {
        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(marginDp);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static String letter(int index) {
        return String.valueOf((char) ('A' + index));
    }

    private void hideKeyboard() {
        View focused = getCurrentFocus();
        if (focused == null) return;
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(focused.getWindowToken(), 0);
        focused.clearFocus();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
