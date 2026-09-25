package za.ac.spu.sacns.activities;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;

import za.ac.spu.sacns.R;

public class MainActivity extends AppCompatActivity {

    private LinearLayout subjectContainer;
    private LinearLayout calculateButton;
    private TextView addSubjectButton;

    private final String[] subjects = {
            "Accounting",
            "Agricultural Management Practices",
            "Agricultural Sciences",
            "Agricultural Technology",
            "Afrikaans (FAL)",
            "Afrikaans (HL)",
            "Business Studies",
            "Civil Technology",
            "Computer Applications Technology",
            "Consumer Studies",
            "Dance Studies",
            "Design",
            "Dramatic Arts",
            "Economics",
            "Electrical Technology",
            "Engineering Graphics and Design",
            "English (FAL)",
            "English (HL)",
            "Geography",
            "History",
            "Information Technology",
            "IsiNdebele (FAL)",
            "IsiNdebele (HL)",
            "IsiXhosa (FAL)",
            "IsiXhosa (HL)",
            "IsiZulu (FAL)",
            "IsiZulu (HL)",
            "Life Sciences",
            "Life Orientation",
            "Marine Sciences",
            "Mathematical Literacy",
            "Mathematics",
            "Mechanical Technology",
            "Music",
            "Physical Sciences",
            "Religion Studies",
            "Sepedi (FAL)",
            "Sepedi (HL)",
            "Sesotho (FAL)",
            "Sesotho (HL)",
            "Setswana (FAL)",
            "Setswana (HL)",
            "SiSwati (FAL)",
            "SiSwati (HL)",
            "South African Sign Language (HL)",
            "Technical Mathematics",
            "Technical Sciences",
            "Tourism",
            "Tshivenda (FAL)",
            "Tshivenda (HL)",
            "Visual Arts",
            "XiTsonga (FAL)",
            "XiTsonga (HL)"
    };

    private final String[] levels = {
            "Select level",
            "Level 1 (0-29%)",
            "Level 2 (30-39%)",
            "Level 3 (40-49%)",
            "Level 4 (50-59%)",
            "Level 5 (60-69%)",
            "Level 6 (70-79%)",
            "Level 7 (80-100%)"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(
                getWindow(),
                true
        );

        getWindow().setStatusBarColor(Color.WHITE);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );

        setContentView(R.layout.activity_main);

        View rootLayout = findViewById(R.id.rootLayout);

        ViewCompat.setOnApplyWindowInsetsListener(
                rootLayout,
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    v.setPadding(
                            v.getPaddingLeft(),
                            systemBars.top,
                            v.getPaddingRight(),
                            v.getPaddingBottom()
                    );

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(rootLayout);

        subjectContainer =
                findViewById(R.id.subjectContainer);

        calculateButton =
                findViewById(R.id.calculateButton);

        addSubjectButton =
                findViewById(R.id.addSubjectButton);

        TextView firstSubject =
                findViewById(R.id.subjectSpinner);

        Spinner firstLevelSpinner =
                findViewById(R.id.levelSpinner);

        LinearLayout firstRow =
                findViewById(R.id.subjectRow);

        setupLevelSpinner(firstLevelSpinner);

        setupSubjectSearch(
                firstSubject,
                firstRow
        );

        addSubjectButton.setOnClickListener(
                v -> addSubject()
        );

        calculateButton.setOnClickListener(
                v -> calculateAPS()
        );
    }

    private void setupLevelSpinner(Spinner spinner) {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        levels
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(adapter);
    }

    private void setupSubjectSearch(
            TextView subjectText,
            LinearLayout currentRow) {

        subjectText.setOnClickListener(
                v -> showSubjectSearchDialog(
                        subjectText,
                        currentRow
                )
        );
    }

    private void showSubjectSearchDialog(
            TextView subjectText,
            LinearLayout currentRow) {

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Select Subject")
                        .create();

        LinearLayout mainLayout =
                new LinearLayout(this);

        mainLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        mainLayout.setPadding(
                dpToPx(20),
                dpToPx(8),
                dpToPx(20),
                dpToPx(15)
        );

        EditText searchBox =
                new EditText(this);

        searchBox.setHint(
                "Search for a subject..."
        );

        searchBox.setSingleLine(true);

        searchBox.setTextSize(15);

        searchBox.setTextColor(
                Color.rgb(30, 30, 30)
        );

        searchBox.setHintTextColor(
                Color.rgb(120, 120, 120)
        );

        searchBox.setPadding(
                dpToPx(12),
                0,
                dpToPx(12),
                0
        );

        searchBox.setBackgroundResource(
                R.drawable.rounded_input
        );

        mainLayout.addView(
                searchBox,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(50)
                )
        );

        LinearLayout subjectList =
                new LinearLayout(this);

        subjectList.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout.LayoutParams listParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(360)
                );

        listParams.topMargin =
                dpToPx(12);

        mainLayout.addView(
                subjectList,
                listParams
        );

        updateSubjectList(
                subjectList,
                "",
                subjectText,
                currentRow,
                dialog
        );

        searchBox.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        updateSubjectList(
                                subjectList,
                                s.toString(),
                                subjectText,
                                currentRow,
                                dialog
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );

        dialog.setView(mainLayout);

        dialog.show();

        if (dialog.getWindow() != null) {

            dialog.getWindow().setLayout(
                    (int) (
                            getResources()
                                    .getDisplayMetrics()
                                    .widthPixels
                                    * 0.92
                    ),
                    (int) (
                            getResources()
                                    .getDisplayMetrics()
                                    .heightPixels
                                    * 0.68
                    )
            );
        }
    }

    private void updateSubjectList(
            LinearLayout subjectList,
            String searchText,
            TextView subjectText,
            LinearLayout currentRow,
            AlertDialog dialog) {

        subjectList.removeAllViews();

        String search =
                searchText
                        .toLowerCase()
                        .trim();

        boolean found = false;

        for (String subject : subjects) {

            if (!subject
                    .toLowerCase()
                    .contains(search)) {

                continue;
            }

            found = true;

            TextView subjectItem =
                    new TextView(this);

            subjectItem.setText(subject);

            subjectItem.setTextColor(
                    Color.rgb(30, 30, 30)
            );

            subjectItem.setTextSize(14);

            subjectItem.setGravity(
                    Gravity.CENTER_VERTICAL
            );

            subjectItem.setPadding(
                    dpToPx(12),
                    0,
                    dpToPx(12),
                    0
            );

            subjectItem.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_book
                    ),
                    null,
                    null,
                    null
            );

            subjectItem.setCompoundDrawablePadding(
                    dpToPx(10)
            );

            subjectItem.setBackgroundResource(
                    R.drawable.rounded_subject_item
            );

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dpToPx(48)
                    );

            params.bottomMargin =
                    dpToPx(7);

            subjectList.addView(
                    subjectItem,
                    params
            );

            subjectItem.setOnClickListener(
                    v -> {

                        if (isSubjectAlreadySelected(
                                subject,
                                currentRow)) {

                            Toast.makeText(
                                    MainActivity.this,
                                    "This subject has already been selected.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        if (isFAL(subject)
                                && isFALAlreadySelected(
                                currentRow)) {

                            Toast.makeText(
                                    MainActivity.this,
                                    "You can only select one FAL language.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        if (isHL(subject)
                                && isHLAlreadySelected(
                                currentRow)) {

                            Toast.makeText(
                                    MainActivity.this,
                                    "You can only select one HL language.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        subjectText.setText(subject);

                        subjectText.setTextColor(
                                Color.rgb(35, 35, 35)
                        );

                        subjectText.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_book,
                                0,
                                0,
                                0
                        );

                        subjectText.setCompoundDrawablePadding(
                                dpToPx(8)
                        );

                        dialog.dismiss();
                    }
            );
        }

        if (!found) {

            TextView noResults =
                    new TextView(this);

            noResults.setText(
                    "No subjects found"
            );

            noResults.setTextColor(
                    Color.rgb(120, 120, 120)
            );

            noResults.setTextSize(14);

            noResults.setGravity(
                    Gravity.CENTER
            );

            subjectList.addView(
                    noResults,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dpToPx(80)
                    )
            );
        }
    }

    private boolean isLanguageSubject(
            String subject) {

        return isFAL(subject) || isHL(subject);
    }

    private boolean isFAL(
            String subject) {

        return subject.endsWith("(FAL)");
    }

    private boolean isHL(
            String subject) {

        return subject.endsWith("(HL)");
    }

    private boolean isSubjectAlreadySelected(
            String subject,
            View currentRow) {

        for (int i = 0;
             i < subjectContainer.getChildCount();
             i++) {

            View row =
                    subjectContainer.getChildAt(i);

            if (row == currentRow) {
                continue;
            }

            if (!(row instanceof LinearLayout)) {
                continue;
            }

            LinearLayout layout =
                    (LinearLayout) row;

            if (layout.getChildCount() > 1
                    && layout.getChildAt(1)
                    instanceof TextView) {

                TextView existingSubjectText =
                        (TextView) layout.getChildAt(1);

                String existingSubject =
                        existingSubjectText
                                .getText()
                                .toString()
                                .trim();

                if (existingSubject.equals(subject)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isFALAlreadySelected(
            View currentRow) {

        for (int i = 0;
             i < subjectContainer.getChildCount();
             i++) {

            View row =
                    subjectContainer.getChildAt(i);

            if (row == currentRow) {
                continue;
            }

            if (!(row instanceof LinearLayout)) {
                continue;
            }

            LinearLayout layout =
                    (LinearLayout) row;

            if (layout.getChildCount() > 1
                    && layout.getChildAt(1)
                    instanceof TextView) {

                TextView subjectText =
                        (TextView) layout.getChildAt(1);

                String existingSubject =
                        subjectText
                                .getText()
                                .toString()
                                .trim();

                if (isFAL(existingSubject)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isHLAlreadySelected(
            View currentRow) {

        for (int i = 0;
             i < subjectContainer.getChildCount();
             i++) {

            View row =
                    subjectContainer.getChildAt(i);

            if (row == currentRow) {
                continue;
            }

            if (!(row instanceof LinearLayout)) {
                continue;
            }

            LinearLayout layout =
                    (LinearLayout) row;

            if (layout.getChildCount() > 1
                    && layout.getChildAt(1)
                    instanceof TextView) {

                TextView subjectText =
                        (TextView) layout.getChildAt(1);

                String existingSubject =
                        subjectText
                                .getText()
                                .toString()
                                .trim();

                if (isHL(existingSubject)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void addSubject() {

        int rowNumber =
                subjectContainer
                        .getChildCount()
                        + 1;

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        row.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(62)
                )
        );

        TextView numberText =
                new TextView(this);

        numberText.setText(
                String.valueOf(rowNumber)
        );

        numberText.setTextColor(
                Color.rgb(21, 91, 135)
        );

        numberText.setTextSize(14);

        numberText.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams numberParams =
                new LinearLayout.LayoutParams(
                        dpToPx(28),
                        dpToPx(58)
                );

        row.addView(
                numberText,
                numberParams
        );

        TextView subjectText =
                new TextView(this);

        subjectText.setText(
                "Search subject..."
        );

        subjectText.setTextColor(
                Color.rgb(119, 119, 119)
        );

        subjectText.setTextSize(13);

        subjectText.setGravity(
                Gravity.CENTER_VERTICAL
        );

        subjectText.setPadding(
                dpToPx(10),
                0,
                dpToPx(8),
                0
        );

        Drawable searchIcon =
                ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_search
                );

        subjectText.setCompoundDrawablesWithIntrinsicBounds(
                searchIcon,
                null,
                null,
                null
        );

        subjectText.setCompoundDrawablePadding(
                dpToPx(8)
        );

        subjectText.setBackgroundResource(
                R.drawable.rounded_input
        );

        subjectText.setClickable(true);

        subjectText.setFocusable(true);

        LinearLayout.LayoutParams subjectParams =
                new LinearLayout.LayoutParams(
                        0,
                        dpToPx(52),
                        1.25f
                );

        row.addView(
                subjectText,
                subjectParams
        );

        Spinner levelSpinner =
                new Spinner(this);

        setupLevelSpinner(levelSpinner);

        LinearLayout.LayoutParams levelParams =
                new LinearLayout.LayoutParams(
                        0,
                        dpToPx(52),
                        0.95f
                );

        levelParams.setMargins(
                dpToPx(10),
                0,
                0,
                0
        );

        row.addView(
                levelSpinner,
                levelParams
        );

        ImageButton deleteButton =
                new ImageButton(this);

        deleteButton.setImageResource(
                android.R.drawable.ic_menu_delete
        );

        deleteButton.setColorFilter(
                Color.rgb(21, 91, 135)
        );

        deleteButton.setBackgroundColor(
                Color.TRANSPARENT
        );

        deleteButton.setPadding(
                dpToPx(9),
                dpToPx(9),
                dpToPx(9),
                dpToPx(9)
        );

        deleteButton.setContentDescription(
                "Delete subject"
        );

        LinearLayout.LayoutParams deleteParams =
                new LinearLayout.LayoutParams(
                        dpToPx(38),
                        dpToPx(52)
                );

        row.addView(
                deleteButton,
                deleteParams
        );

        deleteButton.setOnClickListener(
                v -> {

                    subjectContainer.removeView(row);

                    renumberRows();
                }
        );

        setupSubjectSearch(
                subjectText,
                row
        );

        subjectContainer.addView(row);
    }

    private void renumberRows() {

        for (int i = 0;
             i < subjectContainer.getChildCount();
             i++) {

            View view =
                    subjectContainer.getChildAt(i);

            if (!(view instanceof LinearLayout)) {
                continue;
            }

            LinearLayout row =
                    (LinearLayout) view;

            if (row.getChildCount() > 0
                    && row.getChildAt(0)
                    instanceof TextView) {

                TextView numberText =
                        (TextView) row.getChildAt(0);

                numberText.setText(
                        String.valueOf(i + 1)
                );
            }
        }
    }

    private void calculateAPS() {

        ArrayList<Integer> scores =
                new ArrayList<>();

        for (int i = 0;
             i < subjectContainer.getChildCount();
             i++) {

            View view =
                    subjectContainer.getChildAt(i);

            if (!(view instanceof LinearLayout)) {
                continue;
            }

            LinearLayout row =
                    (LinearLayout) view;

            if (row.getChildCount() < 3) {
                continue;
            }

            TextView subjectText =
                    (TextView) row.getChildAt(1);

            Spinner levelSpinner =
                    (Spinner) row.getChildAt(2);

            String subject =
                    subjectText
                            .getText()
                            .toString();

            String level =
                    levelSpinner
                            .getSelectedItem()
                            .toString();

            if (subject.equals(
                    "Search subject...")
                    || subject.trim().isEmpty()
                    || level.equals(
                    "Select level")) {

                continue;
            }

            int points;

            if (subject.equals(
                    "Life Orientation")) {

                points =
                        getLifeOrientationPoints(
                                level
                        );

            } else {

                points =
                        getBasePoints(
                                level
                        );

                if (subject.equals(
                        "Mathematics")
                        || isHigherLanguage(
                        subject)) {

                    points +=
                            getAdditionalPoints(
                                    level
                            );
                }
            }

            scores.add(points);
        }

        if (scores.size() < 6) {

            Toast.makeText(
                    this,
                    "Please enter at least 6 subjects and their achievement levels.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        Collections.sort(
                scores,
                Collections.reverseOrder()
        );

        int aps = 0;

        for (int i = 0; i < 6; i++) {
            aps += scores.get(i);
        }

        showAPSResult(aps);
    }

    private void showAPSResult(
            int aps) {

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Your APS Result")
                        .setMessage(
                                "Your SPU Admission Point Score is:\n\n"
                                        + aps
                        )
                        .setPositiveButton(
                                "Done",
                                null
                        )
                        .create();

        dialog.show();
    }

    private boolean isHigherLanguage(
            String subject) {

        return subject.equals(
                "Afrikaans (HL)"
        )
                || subject.equals(
                "English (HL)"
        )
                || subject.equals(
                "IsiNdebele (HL)"
        )
                || subject.equals(
                "IsiXhosa (HL)"
        )
                || subject.equals(
                "IsiZulu (HL)"
        )
                || subject.equals(
                "Sepedi (HL)"
        )
                || subject.equals(
                "Sesotho (HL)"
        )
                || subject.equals(
                "Setswana (HL)"
        )
                || subject.equals(
                "SiSwati (HL)"
        )
                || subject.equals(
                "Tshivenda (HL)"
        )
                || subject.equals(
                "XiTsonga (HL)"
        )
                || subject.equals(
                "South African Sign Language (HL)"
        );
    }

    private int getBasePoints(
            String level) {

        if (level.equals(
                "Level 7 (80-100%)"
        )) return 7;

        if (level.equals(
                "Level 6 (70-79%)"
        )) return 6;

        if (level.equals(
                "Level 5 (60-69%)"
        )) return 5;

        if (level.equals(
                "Level 4 (50-59%)"
        )) return 4;

        if (level.equals(
                "Level 3 (40-49%)"
        )) return 3;

        if (level.equals(
                "Level 2 (30-39%)"
        )) return 2;

        if (level.equals(
                "Level 1 (0-29%)"
        )) return 1;

        return 0;
    }

    private int getAdditionalPoints(
            String level) {

        if (level.equals(
                "Level 7 (80-100%)"
        )) return 2;

        if (level.equals(
                "Level 6 (70-79%)"
        )) return 2;

        if (level.equals(
                "Level 5 (60-69%)"
        )) return 2;

        if (level.equals(
                "Level 4 (50-59%)"
        )) return 1;

        if (level.equals(
                "Level 3 (40-49%)"
        )) return 1;

        return 0;
    }

    private int getLifeOrientationPoints(
            String level) {

        if (level.equals(
                "Level 7 (80-100%)"
        )) return 3;

        if (level.equals(
                "Level 6 (70-79%)"
        )) return 2;

        if (level.equals(
                "Level 5 (60-69%)"
        )) return 1;

        return 0;
    }

    private int dpToPx(
            int dp) {

        return Math.round(
                dp
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}