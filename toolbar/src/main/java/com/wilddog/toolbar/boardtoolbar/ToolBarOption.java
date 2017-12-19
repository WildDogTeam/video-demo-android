package com.wilddog.toolbar.boardtoolbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.wilddog.board.BoardToolOption;
import com.wilddog.board.ToolType;
import com.wilddog.board.WilddogBoard;
import com.wilddog.board.WilddogBoardObject;
import com.wilddog.board.listener.OnObjectListener;
import com.wilddog.toolbar.R;
import com.wilddog.toolbar.util.BitMapUtil;
import com.wilddog.toolbar.util.GlideImageLoader;
import com.wilddog.toolbar.util.QiniuUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.finalteam.galleryfinal.CoreConfig;
import cn.finalteam.galleryfinal.FunctionConfig;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.ThemeConfig;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import me.leefeng.promptlibrary.PromptDialog;

import static com.wilddog.toolbar.boardtoolbar.ToolBarControllButton.ControllButtonType.CICLE;
import static com.wilddog.toolbar.boardtoolbar.ToolBarControllButton.ControllButtonType.DEL;
import static com.wilddog.toolbar.boardtoolbar.ToolBarControllButton.ControllButtonType.LINE;
import static com.wilddog.toolbar.boardtoolbar.ToolBarControllButton.ControllButtonType.PEN;
import static com.wilddog.toolbar.boardtoolbar.ToolBarControllButton.ControllButtonType.PIC;
import static com.wilddog.toolbar.boardtoolbar.ToolBarControllButton.ControllButtonType.RECT;
import static com.wilddog.toolbar.boardtoolbar.ToolBarControllButton.ControllButtonType.TEXT;
import static com.wilddog.toolbar.boardtoolbar.ToolBarControllButton.ControllButtonType.UNDO;

/**
 * Created by he on 2017/8/30.
 */

public class ToolBarOption implements View.OnClickListener, OnObjectListener {

    private static final int SELECTION = 245;
    private static final int UPDATE = 497;

    private static final int SMALL_TEXT = 18;
    private static final int SMALL_PEN = 2;
    private static final int MID_TEXT = 28;
    private static final int MID_PEN = 6;
    private static final int BIG_TEXT = 40;
    private static final int BIG_PEN = 10;

    static final int RED_COLOR = Color.rgb(252,61,57);
    static final int YELLOW_COLOR = Color.rgb(252,148,39);
    static final int GREEN_COLOR = Color.rgb(81,214,106);
    static final int BLUE_COLOR = Color.rgb(21,128,249);
    static final int GRAY_COLOR = Color.rgb(202,202,202);
    static final int BLACK_COLOR = Color.rgb(10,10,10);
    public static final String FONT_SIZE = "fontSize";
    public static final String STROKE_WIDTH = "strokeWidth";

    private WilddogBoard mBoardView;
    private Context mContext;
    private ToolBarMenu mActionsMenu;
    int mColor = RED_COLOR;
    ToolType mToolType;
    float mSize;
    private ParamHashMap mParamsMap;

    private int currentMode = UPDATE;
    private WilddogBoardObject mBoardObject;

    private Map<ToolBarControllButton, SaveState> states = new HashMap<>();
    private ToolBarControllButton mCurrentControllButton;
    private PromptDialog promptDialog;

    public ToolBarOption(ToolBarMenu mActionsMenu) {

        this.mActionsMenu = mActionsMenu;

        mParamsMap = new ParamHashMap().add(SMALL_TEXT, 0).add(SMALL_PEN, 0)
                .add(MID_TEXT, 1).add(MID_PEN, 1)
                .add(BIG_PEN, 2).add(BIG_TEXT, 2)
                .add(RED_COLOR, 3).add(YELLOW_COLOR, 4)
                .add(GREEN_COLOR, 5).add(BLUE_COLOR, 6)
                .add(GRAY_COLOR, 7).add(BLACK_COLOR, 8);
    }


    @Override
    public void onClick(View v) {

        if (currentMode == SELECTION) {
            selectionClickOption(v);
            saveSelectState(v);
        } else if (currentMode == UPDATE) {
            updateClickOption(v);
        }


    }

    private void saveSelectState(View v) {
        SaveState saveState = new SaveState();
        if (states.containsKey(mCurrentControllButton)) {
            saveState = states.get(mCurrentControllButton);
        }
        saveState.saveState(v);
        states.put(mCurrentControllButton, saveState);
    }

    /**
     * UPDATE模式下floatingbutton的点击事件
     * @param v
     */
    private void updateClickOption(View v) {
        Map<String, Object> map = new HashMap<>();
        int vId = v.getId();
        if (vId == R.id.fab_small) {
            if (mBoardObject.getType() == WilddogBoardObject.BoardObjectType.OBJECTTEXT) {
                map.put(FONT_SIZE, SMALL_TEXT);
            } else {
                map.put(STROKE_WIDTH, SMALL_PEN);
            }
            mBoardObject.updateProperties(map);
//                mBoardView.removeObject(mBoardObject.getObjectId());
//                mBoardObject.setSize(20);
//                mBoardView.addObject(mBoardObject);

        } else if (vId == R.id.fab_middle) {
            if (mBoardObject.getType() == WilddogBoardObject.BoardObjectType.OBJECTTEXT) {
                map.put(FONT_SIZE, MID_TEXT);
            } else {
                map.put(STROKE_WIDTH, SMALL_PEN);
            }
            mBoardObject.updateProperties(map);

        } else if (vId == R.id.fab_big) {
            if (mBoardObject.getType() == WilddogBoardObject.BoardObjectType.OBJECTTEXT) {
                map.put(FONT_SIZE, BIG_TEXT);
            } else {
                map.put(STROKE_WIDTH, BIG_PEN);
            }
            mBoardObject.updateProperties(map);

        } else if (vId == R.id.fab_red) {
            mColor = RED_COLOR;
            updateColor();

        } else if (vId == R.id.fab_yellow) {
            mColor = YELLOW_COLOR;
            updateColor();

        } else if (vId == R.id.fab_green) {
            mColor = GREEN_COLOR;
            updateColor();

        } else if (vId == R.id.fab_blue) {
            mColor = BLUE_COLOR;
            updateColor();

        } else if (vId == R.id.fab_gray) {
            mColor = GRAY_COLOR;
            updateColor();

        } else if (vId == R.id.fab_black) {
            mColor = BLACK_COLOR;
            updateColor();

        }
        updateButtonState(v);
    }

    private void updateColor() {
        Map<String, Object> map = new HashMap<>();
        if (mBoardObject.getType() == WilddogBoardObject.BoardObjectType.OBJECTTEXT) {
            map.put("fill", getRgbaValue());
        } else {
            map.put("stroke", getRgbaValue());
        }
        mBoardObject.updateProperties(map);
    }


    /**
     * SELECTING模式下floatingbutton的点击事件
     * @param v
     */
    private void selectionClickOption(View v) {
        int vId = v.getId();
        if (vId == R.id.fab_small) {
            if (mToolType == ToolType.TEXT)
                mSize = SMALL_TEXT;
            else
                mSize = SMALL_PEN;

        } else if (vId == R.id.fab_middle) {
            if (mToolType == ToolType.TEXT)
                mSize = MID_TEXT;
            else
                mSize = MID_PEN;

        } else if (vId == R.id.fab_big) {
            if (mToolType == ToolType.TEXT)
                mSize = BIG_TEXT;
            else
                mSize = BIG_PEN;

        } else if (vId == R.id.fab_red) {
            mColor = RED_COLOR;

        } else if (vId == R.id.fab_yellow) {
            mColor = YELLOW_COLOR;

        } else if (vId == R.id.fab_green) {
            mColor = GREEN_COLOR;

        } else if (vId == R.id.fab_blue) {
            mColor = BLUE_COLOR;

        } else if (vId == R.id.fab_gray) {
            mColor = GRAY_COLOR;

        } else if (vId == R.id.fab_black) {
            mColor = BLACK_COLOR;

        }
        mBoardView.setTool(mToolType, new BoardToolOption(mColor, mSize, 0f));

        updateButtonState(v);
    }

    /**
     * 更新floatingbutton的选中状态
     * @param v
     */
    private void updateButtonState(View v) {
        if (v.isSelected()) {
//            v.setSelected(false);
        } else {
            v.setSelected(true);
            clearOthersSelectedState(v);
        }
    }


    private void clearOthersSelectedState(View v) {
        mActionsMenu.clearOthersSelectedState((ToolBarFloatingButton) v);
    }


    /**
     * 设置监听事件
     */
    private void setListeners() {
        mActionsMenu.setOnFloatingActionsMenuClickListener(new ToolBarMenu.OnFloatingActionsMenuClickListener() {
            @Override
            public void addButtonLister(ToolBarControllButton controllButton, int controllButtonType) {
                setCurrentMode(SELECTION);
//                mBoardView.setTool(mToolType, new BoardToolOption(mColor, mSize, 0f));

                switch (controllButtonType) {
                    case PEN:
                        mToolType = ToolType.SINGLESYNCPEN;
                        mSize = MID_PEN;
                        break;
                    case LINE:
                        mToolType = ToolType.LINE;
                        mSize = MID_PEN;
                        break;
                    case RECT:
                        mToolType = ToolType.EMPTYRECT;
                        mSize = MID_PEN;
                        break;
                    case CICLE:
                        mToolType = ToolType.EMPTYCICLE;
                        mSize = MID_PEN;
                        break;
                    case PIC:
                        GalleryFinal.openGallerySingle(100, new GalleryFinal.OnHanlderResultCallback() {
                            @Override
                            public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {
                                String photoPath = resultList.get(0).getPhotoPath();
                                uploadPic(photoPath);
                            }

                            @Override
                            public void onHanlderFailure(int requestCode, String errorMsg) {

                            }
                        });
                        break;
                    case TEXT:
                        mToolType = ToolType.TEXT;
                        mSize = MID_TEXT;
                        break;
                    case UNDO:
                        mBoardView.undo();
                        break;
                    case DEL:
                        if (mBoardObject != null)
                            mBoardView.removeObject(mBoardObject);
                        else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setMessage("确认清空吗?");

                            builder.setTitle("提示");

                            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mBoardView.clearPage();
                                }
                            });

                            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            builder.create().show();
                        }
                        break;
                }

                if (!mActionsMenu.isExpanded()) {
                    resetToolDefault();
                    setmCurrentControllButton(null);
                } else {
                    setmCurrentControllButton(controllButton);
                    restoreState(controllButton);

                    mBoardView.setTool(mToolType, new BoardToolOption(RED_COLOR, mSize, 0f));
                }
            }

        });



        mBoardView.addObjectSelectedListener(this);
        mBoardView.addObjectDeSelectedListener(new OnObjectListener() {
            @Override
            public void onComplete(WilddogBoardObject obj, Map<String, Object> propertiesMap) {
                mActionsMenu.deSelectExpand();
                mBoardObject = null;
            }
        });
    }

    private void restoreState(ToolBarControllButton controllButton) {
        SaveState saveState = states.get(controllButton);
        if (saveState != null)
            saveState.restoreState();
        else {
            Integer floatingButtonIndex;
            if (controllButton.getControllButtonType()==TEXT) {
                floatingButtonIndex= mParamsMap.getFloatingButtonIndex(MID_TEXT);
            }else {
                floatingButtonIndex = mParamsMap.getFloatingButtonIndex(MID_PEN);
            }
            Integer redfloatingButtonIndex = mParamsMap.getFloatingButtonIndex(RED_COLOR);
            mActionsMenu.setFloatingButtonSelected(floatingButtonIndex,redfloatingButtonIndex);
        }
    }

    /**
     * 设置当前选中controllbutton
     * @param mCurrentControllButton
     */
    private void setmCurrentControllButton(ToolBarControllButton mCurrentControllButton) {
        this.mCurrentControllButton = mCurrentControllButton;
    }

    /**
     * 设置工具栏模式
     * @param mode
     */
    private void setCurrentMode(int mode) {
        this.currentMode = mode;
    }

    /**
     * 选中图形，弹出工具条并将相关属性选中，同时更新工具栏模式为更新模式
     *
     * @param boardObject
     * @param propertiesMap
     */
    @Override
    public void onComplete(WilddogBoardObject boardObject, Map<String, Object> propertiesMap) {
        this.mBoardObject = boardObject;

        pupMenu();

        setFloatingButtonSelected(propertiesMap);

        resetToolDefault();

        setCurrentMode(UPDATE);

    }

    /**
     * 工具栏重置为默认
     *
     */
    private void resetToolDefault() {
        mSize = 0;
        mColor = BLACK_COLOR;
        mBoardView.setTool(ToolType.DEFAULT, new BoardToolOption(mColor, mSize, 0f));
    }

    /**
     * 选中弹出工具条相关属性
     * @param map
     */
    private void setFloatingButtonSelected(Map<String, Object> map) {
        int sizeIndex = (int) mBoardObject.getSize();
        int sizebuttonIndex = mParamsMap.getFloatingButtonIndex(sizeIndex);
        int colorButtonIndex;
        if (mBoardObject.getType() == WilddogBoardObject.BoardObjectType.OBJECTTEXT)
            colorButtonIndex = mParamsMap.getFloatingButtonIndex(rgb2ColorInt(map.get("fill").toString()));
        else
            colorButtonIndex = mParamsMap.getFloatingButtonIndex(rgb2ColorInt(map.get("stroke").toString()));

        mActionsMenu.setFloatingButtonSelected(sizebuttonIndex, colorButtonIndex);

    }


    private int rgb2ColorInt(String rgba){
        String s=rgba.substring(rgba.indexOf('(')+1,rgba.length()-1);
        String []ssplit=s.split(",");
        int r= Integer.parseInt(ssplit[0].trim());
        int g= Integer.parseInt(ssplit[1].trim());
        int b= Integer.parseInt(ssplit[2].trim());
        Log.e("========", "rgb2ColorInt: "+r+","+g+","+b+"," );

        return Color.rgb(r,g,b);
    }
    /**
     * 工具条弹出
     */
    private void pupMenu() {
        if (mBoardObject.getType() == WilddogBoardObject.BoardObjectType.OBJECTTEXT) {
            mActionsMenu.expandText();
        } else if(mBoardObject.getType()!=WilddogBoardObject.BoardObjectType.OBJECTIMAGE){
            mActionsMenu.expandDot();
        }
        mActionsMenu.clearAllControllSelectedState();
    }

    /**
     * 设置画板，同时初始化相关配置
     * @param boardView
     * @param activity
     */
    public void setBoardView(WilddogBoard boardView, Activity activity) {
        this.mBoardView = boardView;
        this.mContext = activity;
        FunctionConfig functionConfig = new FunctionConfig.Builder()
                .setEnableEdit(false)
//                .setCropWidth(50)
//                .setCropHeight(50)
                .setEnablePreview(true)
                .build();

        CoreConfig coreConfig = new CoreConfig.Builder(activity, new GlideImageLoader(), ThemeConfig.DEFAULT)
                .setNoAnimcation(true)
                .setFunctionConfig(functionConfig).build();
        GalleryFinal.init(coreConfig);

        promptDialog = new PromptDialog(activity);
        //设置自定义属性
//        promptDialog.getDefaultBuilder().touchAble(true).round(3).loadingDuration(3000);
        setListeners();
    }

    /**
     * 上传图片
     * @param image
     */
    private void uploadPic(String image) {
        promptDialog.showLoading("正在上传");
        Log.e("aaa", "uploadPic: " );
        String imagePath = BitMapUtil.getImagePath(image, System.currentTimeMillis() + "");
        QiniuUtil.getInstance().getUploadManager().put(imagePath, System.currentTimeMillis() + "", QiniuUtil.getInstance().getToken(),
                new UpCompletionHandler() {
                    @Override
                    public void complete(String key, ResponseInfo info, JSONObject res) {
                        promptDialog.dismiss();
                        if (info.isOK()) {
                            Log.i("qiniu", "Upload Success");
                            WilddogBoardObject wilddogBoardObject = WilddogBoardObject.creatImage(mBoardView, QiniuUtil.getInstance().getURL(key));
                            mBoardView.addObject(wilddogBoardObject);
                        } else {
                            Log.i("qiniu", "Upload Fail");
                            Log.e("qiniu",info.error);
                        }
                    }
                }, null);
    }

    /**
     * 得到rgba颜色值
     * @return
     */
    private String getRgbaValue() {
        return "rgba(" + Color.red(mColor) +
                "," + Color.green(mColor) +
                "," + Color.blue(mColor) +
                "," + Color.alpha(mColor) +
                ")";
    }

    public int getCurrentMode() {
        return currentMode;
    }


    /**
     * 保存相关controllbutton的状态
     */
    private class SaveState {
        private ToolBarFloatingButton mSizeButton;
        private ToolBarFloatingButton mColorButton;
        private float size;
        private int color;

        void saveState(View v) {
            ToolBarFloatingButton floatingButton = (ToolBarFloatingButton) v;
            if (mCurrentControllButton != null) {
                if (floatingButton.getType() == ToolBarFloatingButton.FloatingType.SIZE) {
                    mSizeButton = floatingButton;
                    size = ToolBarOption.this.mSize;
                } else {
                    mColorButton = floatingButton;
                    color = ToolBarOption.this.mColor;
                }
            }
        }

        void restoreState() {
            if (mSizeButton != null) {
                mSizeButton.setSelected(true);
                ToolBarOption.this.mSize = size;
            }
            if (mColorButton != null) {
                mColorButton.setSelected(true);
                ToolBarOption.this.mColor = color;
            }
        }
    }
}
