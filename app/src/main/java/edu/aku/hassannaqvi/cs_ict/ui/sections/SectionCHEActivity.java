package edu.aku.hassannaqvi.cs_ict.ui.sections;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.validatorcrawler.aliazaz.Validator;

import org.json.JSONException;
import org.json.JSONObject;

import edu.aku.hassannaqvi.cs_ict.R;
import edu.aku.hassannaqvi.cs_ict.contracts.ChildContract;
import edu.aku.hassannaqvi.cs_ict.core.DatabaseHelper;
import edu.aku.hassannaqvi.cs_ict.core.MainApp;
import edu.aku.hassannaqvi.cs_ict.databinding.ActivitySectionChEBinding;
import edu.aku.hassannaqvi.cs_ict.ui.other.ChildEndingActivity;
import edu.aku.hassannaqvi.cs_ict.utils.JSONUtils;

import static edu.aku.hassannaqvi.cs_ict.core.MainApp.child;
import static edu.aku.hassannaqvi.cs_ict.utils.UtilKt.openChildEndActivity;

public class SectionCHEActivity extends AppCompatActivity {

    ActivitySectionChEBinding bi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bi = DataBindingUtil.setContentView(this, R.layout.activity_section_ch_e);
        bi.setCallback(this);

        setupListeners();

    }

    private void setupListeners() {

    }

    private boolean UpdateDB() {
        DatabaseHelper db = MainApp.appInfo.getDbHelper();
        int updcount = db.updatesChildColumn(ChildContract.ChildTable.COLUMN_SCC, child.getsCC());
        if (updcount == 1) {
            return true;
        } else {
            Toast.makeText(this, "Updating Database... ERROR!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void SaveDraft() throws JSONException {

        JSONObject json = new JSONObject();

        json.put("im25", bi.im2501.isChecked() ? "1"
                : bi.im2502.isChecked() ? "2"
                : bi.im2503.isChecked() ? "3"
                : bi.im2504.isChecked() ? "4"
                : "0");

        json.put("im26a", bi.im26a.getText().toString());
        json.put("im26b", bi.im26b.getText().toString());
        json.put("im26c", bi.im26c.getText().toString());
        json.put("im26d", bi.im26d.getText().toString());

        try {
            JSONObject json_merge = JSONUtils.mergeJSONObjects(new JSONObject(child.getsCC()), json);

            child.setsCC(String.valueOf(json_merge));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean formValidation() {
        return Validator.emptyCheckingContainer(this, bi.fldGrpSectionCHE);
    }

    public void BtnContinue() {

        if (formValidation()) {
            try {
                SaveDraft();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (UpdateDB()) {
                finish();
                startActivity(new Intent(this, ChildEndingActivity.class).putExtra("complete", true));

            } else {
                Toast.makeText(this, "Failed to Update Database!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void BtnEnd() {
        openChildEndActivity(this);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Back Press Not Allowed", Toast.LENGTH_SHORT).show();
    }


}
