package ru.kuchanov.tproger.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import ru.kuchanov.tproger.R;
import ru.kuchanov.tproger.utils.html.CodeRepresenter;

public class FragmentDialogCodeRepresenter extends DialogFragment
{
    public final static String LOG = FragmentDialogCodeRepresenter.class.getSimpleName();
    private final static String KEY_CODE_LINES = "KEY_CODE_LINES";
    ArrayList<String> codeLines;
    private SharedPreferences pref;
    private Context ctx;

    public static FragmentDialogCodeRepresenter newInstance(ArrayList<String> codeLines)
    {
        FragmentDialogCodeRepresenter fragmentDialogCodeRepresenter = new FragmentDialogCodeRepresenter();
        Bundle b = new Bundle();
        b.putStringArrayList(KEY_CODE_LINES, codeLines);
        fragmentDialogCodeRepresenter.setArguments(b);
        return fragmentDialogCodeRepresenter;
    }

    @Override
    public void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);
        Log.i(LOG, "onCreate");
        this.ctx = this.getActivity();

        this.pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        this.codeLines = this.getArguments().getStringArrayList(KEY_CODE_LINES);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Log.i(LOG, "onCreateDialog");

        float artTextScale = pref.getFloat(ctx.getString(R.string.pref_design_key_text_size_article), 0.75f);
        int textSizePrimary = ctx.getResources().getDimensionPixelSize(R.dimen.text_size_primary);
        float scaledTextSizePrimary = artTextScale * textSizePrimary;

        final MaterialDialog dialog;

        MaterialDialog.Builder builder = new MaterialDialog.Builder(ctx);
        builder.title("Код")
                .positiveText(R.string.close)
                .neutralText(R.string.copy)
                .onNeutral(new MaterialDialog.SingleButtonCallback()
                {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
                    {
                        StringBuilder builder = new StringBuilder();
                        for (String codeLine : codeLines)
                        {
                            builder.append(Html.fromHtml(codeLine).toString()).append("\n");
                        }
                        ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("code", builder.toString());
                        clipboard.setPrimaryClip(clip);
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback()
                {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
                    {
                        materialDialog.dismiss();
                    }
                })
                .autoDismiss(false)
                .customView(R.layout.fragment_dialog_code, true);

        dialog = builder.build();

        View customView = dialog.getCustomView();

        if (customView == null)
        {
            return dialog;
        }

        CodeRepresenter tableContent = new CodeRepresenter(codeLines);
        LinearLayout content = (LinearLayout) customView.findViewById(R.id.content);
        for (int i = 0; i < tableContent.getLines().size(); i++)
        {
            String codeLine = tableContent.getLines().get(i);
//                    Log.i(LOG, codeLine);
            LinearLayout codeLineLayout = (LinearLayout) LayoutInflater.from(ctx).inflate(R.layout.recycler_article_code_representer_code_line, content, false);

            TextView lineNumber = (TextView) codeLineLayout.findViewById(R.id.line_number);
            String lineNumberString = String.valueOf(i + 1);
            String additionalSpacesAfterLineNumber = "";
            String finalLineNumber = String.valueOf(tableContent.getLines().size());
            for (int u = lineNumberString.length(); u < finalLineNumber.length(); u++)
            {
                if (additionalSpacesAfterLineNumber.length() < finalLineNumber.length())
                {
                    additionalSpacesAfterLineNumber += "  ";
                }
            }
            String number = " " + lineNumberString + " " + additionalSpacesAfterLineNumber;
            lineNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSizePrimary);
            lineNumber.setText(number);

            TextView lineCode = (TextView) codeLineLayout.findViewById(R.id.line_code);
            lineCode.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledTextSizePrimary);
            lineCode.setText(Html.fromHtml(codeLine));

            //each second line must have darker color
            if (i % 2 != 0)
            {
                codeLineLayout.setBackgroundResource(R.color.material_teal_200);
                lineNumber.setBackgroundResource(R.color.material_teal_400);
                lineCode.setBackgroundResource(R.color.material_teal_200);
            }

            content.addView(codeLineLayout);
        }

        return dialog;
    }
}