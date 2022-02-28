package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;

public class MaterialSearchView extends FrameLayout {
    public EditText editSearchPdfText;
    public View parent;
    public CharSequence charSequence;
    public Context context;
    public ImageView imgSearchClose;
    public ImageView imgSearchPdf;
    public ConstraintLayout laySearchBarPdf;
    public OnQueryTextListener onQueryTextListener;

    public MaterialSearchView(Context context2) {
        super(context2);
        context = context2;
        init();
    }

    public MaterialSearchView(Context context2, AttributeSet attributeSet) {
        super(context2, attributeSet);
        context = context2;
        init();
    }

    public MaterialSearchView(Context context2, AttributeSet attributeSet, int i) {
        super(context2, attributeSet, i);
        context = context2;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.material_search_view, (ViewGroup) this, true);

        laySearchBarPdf = (ConstraintLayout) findViewById(R.id.laySearchBarPdf);
        laySearchBarPdf.setVisibility(View.GONE);
        parent = (View) laySearchBarPdf.getParent();
        editSearchPdfText = (EditText) laySearchBarPdf.findViewById(R.id.editSearchPdfText);
        imgSearchPdf = (ImageView) laySearchBarPdf.findViewById(R.id.imgSearchPdf);
        imgSearchClose = (ImageView) laySearchBarPdf.findViewById(R.id.imgSearchClose);

        editSearchPdfText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View view, boolean z) {
                if (z) {
                    showKeyboard(editSearchPdfText);
                }
            }
        });


        imgSearchPdf.setOnClickListener(view -> closeSearchingPdfData());

        imgSearchClose.setOnClickListener(view -> {
            editSearchPdfText.setText("");
            imgSearchClose.setVisibility(View.GONE);
        });

        editSearchPdfText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable editable) {
            }

            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                onTextChangedA(charSequence);
            }
        });
    }

    public void onTextChangedA(CharSequence charSequence2) {
        charSequence = editSearchPdfText.getText();
        if (!TextUtils.isEmpty(charSequence)) {
            imgSearchClose.setVisibility(View.VISIBLE);
        } else {
            imgSearchClose.setVisibility(View.GONE);
        }
        if (onQueryTextListener != null) {
            onQueryTextListener.onQueryTextChange(charSequence2.toString());
        }
    }

    public void openPdfSearchData() {
        editSearchPdfText.setText("");
        editSearchPdfText.requestFocus();
        if (Build.VERSION.SDK_INT >= 21) {
            laySearchBarPdf.setVisibility(View.VISIBLE);
        } else {
            laySearchBarPdf.setVisibility(View.VISIBLE);
        }
    }

    public void closeSearchingPdfData() {
        laySearchBarPdf.setVisibility(View.GONE);
        editSearchPdfText.setText("");
        editSearchPdfText.clearFocus();
        hideKeyboard(editSearchPdfText);
    }

    public boolean isSearchOpen() {
        return laySearchBarPdf.getVisibility() == VISIBLE;
    }

    private boolean isHardKeyboardAvailable() {
        return context.getResources().getConfiguration().keyboard != 1;
    }

    @SuppressLint("WrongConstant")
    public void showKeyboard(View view) {
        if (Build.VERSION.SDK_INT <= 10 && view.hasFocus()) {
            view.clearFocus();
        }
        view.requestFocus();
        if (!isHardKeyboardAvailable()) {
            ((InputMethodManager) view.getContext().getSystemService("input_method")).showSoftInput(view, 0);
        }
    }

    @SuppressLint("WrongConstant")
    private void hideKeyboard(View view) {
        ((InputMethodManager) view.getContext().getSystemService("input_method")).hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void setOnQueryTextListener(OnQueryTextListener onQueryTextListener2) {
        onQueryTextListener = onQueryTextListener2;
    }

    public void clearFocus() {
        hideKeyboard(this);
        super.clearFocus();
        editSearchPdfText.clearFocus();
    }

    public interface OnQueryTextListener {
        boolean onQueryTextChange(String str);

        boolean onQueryTextSubmit(String str);
    }
}