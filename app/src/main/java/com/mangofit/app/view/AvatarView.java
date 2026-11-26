package com.mangofit.app.view;

import android.content.Context;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

import com.mangofit.app.R;
import com.mangofit.app.data.ArchivedMango;
import com.mangofit.app.data.Equipment;
import com.mangofit.app.data.GameRepository;
import com.mangofit.app.logic.Levels;
import com.mangofit.app.logic.MangoStage;
import com.mangofit.app.ui.MangoFace;

/**
 * 프로필 사진. 동그란 틀 안에 망고의 얼굴 부분만 크게 보여준다 (입은 옷 포함).
 * 표정은 지금 망고의 표정을 따라간다 (하트 반응 중이면 하트, 운동 직후면 활짝 웃음).
 * 고른 사진이 없으면 기본 사람 아이콘.
 */
public class AvatarView extends ViewGroup {

    /** 망고를 틀보다 이만큼 크게 그려서 얼굴만 보이게 한다 */
    private static final float ZOOM = 1.2f;
    /** 망고 그림 높이에서 얼굴 중심이 있는 위치 (위에서부터 비율) */
    private static final float FACE_CENTER = 0.37f;

    private final ImageView person;
    private final MangoView mango;
    private final Runnable rebindFace = new Runnable() {
        @Override
        public void run() {
            bindFace();
        }
    };
    private GameRepository repo;

    public AvatarView(Context context) {
        this(context, null);
    }

    public AvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundResource(R.drawable.bg_avatar);
        setClipToOutline(true);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
        setContentDescription(context.getString(R.string.user_icon));

        person = new ImageView(context);
        person.setImageResource(R.drawable.ic_person);
        person.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        addView(person);

        mango = new MangoView(context);
        mango.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        mango.setVisibility(GONE);
        addView(mango);
    }

    /** 저장된 프로필 사진으로 그린다 */
    public void bind(GameRepository repo) {
        bind(repo, repo.getAvatar());
    }

    /** 고르기 화면에서 후보를 그릴 때 */
    public void bind(GameRepository repo, String avatar) {
        this.repo = repo;
        Equipment equipment = null;
        int level = 1;
        if (avatar.startsWith("base:")) {
            try {
                equipment = Equipment.empty();
                level = MangoStage.valueOf(avatar.substring(5)).minLevel;
            } catch (IllegalArgumentException e) {
                equipment = null;
            }
        } else if (avatar.equals("current")) {
            equipment = repo.getEquipment();
            level = repo.getMangoProgress().level;
        } else if (avatar.startsWith("mango:")) {
            try {
                ArchivedMango m = repo.getArchivedMango(Integer.parseInt(avatar.substring(6)));
                if (m != null) {
                    equipment = m.equipped;
                    level = Levels.levelForExp(m.getExp());
                }
            } catch (NumberFormatException ignored) {
                // 잘못 저장된 값이면 기본 아이콘
            }
        }
        boolean hasMango = equipment != null;
        person.setVisibility(hasMango ? GONE : VISIBLE);
        mango.setVisibility(hasMango ? VISIBLE : GONE);
        if (hasMango) mango.setAppearance(equipment, level);
        bindFace();
    }

    private void bindFace() {
        removeCallbacks(rebindFace);
        if (repo == null || mango.getVisibility() != VISIBLE) return;
        long love = repo.loveRemaining();
        long grin = repo.workoutGrinRemaining();
        if (love > 0) {
            mango.setFace(MangoFace.LOVE);
            postDelayed(rebindFace, love);
        } else if (grin > 0) {
            mango.setFace(MangoFace.GRIN);
            postDelayed(rebindFace, grin);
        } else {
            mango.setFace(MangoFace.SMILE);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(rebindFace);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        bindFace();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int h = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(w, h);
        int icon = Math.round(Math.min(w, h) * 0.58f);
        person.measure(exactly(icon), exactly(icon));
        int size = Math.round(Math.min(w, h) * ZOOM);
        mango.measure(exactly(size), exactly(size));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int w = r - l;
        int h = b - t;
        int pw = person.getMeasuredWidth();
        person.layout((w - pw) / 2, (h - pw) / 2, (w + pw) / 2, (h + pw) / 2);
        int size = mango.getMeasuredWidth();
        int left = (w - size) / 2;
        // 정사각형 안에 망고가 가운데 정렬로 그려지므로, 얼굴 중심이 틀 가운데에 오도록 올린다
        int top = Math.round(h / 2f - size * FACE_CENTER);
        mango.layout(left, top, left + size, top + size);
    }

    private static int exactly(int size) {
        return MeasureSpec.makeMeasureSpec(Math.max(0, size), MeasureSpec.EXACTLY);
    }
}
