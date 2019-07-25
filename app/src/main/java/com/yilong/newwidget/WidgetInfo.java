package com.yilong.newwidget;

import java.util.ArrayList;
import java.util.Collections;

public enum WidgetInfo {
    ZK_CHECK_IN(R.string.zk_check_in, R.mipmap.unselected_check_in, R.mipmap.selected_check_in, true, false, -1),
    ZK_CHECK_OUT(R.string.zk_check_out, R.mipmap.unselected_check_out, R.mipmap.selected_check_out, true, false, -1),
    ZK_GO_OUT(R.string.zk_go_out, R.mipmap.unselected_go_out, R.mipmap.selected_go_out, true, false, -1),
    ZK_GoBACK(R.string.zk_goback, R.mipmap.unselected_goback, R.mipmap.selected_goback, true, false, -1),
    ZK_OVERTIME_CHECK_IN(R.string.zk_overtime_check_in, R.mipmap.unselected_overtime_check_in, R.mipmap.selected_overtime_check_in, true, false, -1),
    ZK_OVERTIME_CHECK_OUT(R.string.zk_overtime_check_out, R.mipmap.unselected_overtime_check_out, R.mipmap.selected_overtime_check_out, true, false, -1);

    private int nameId;
    private int unselectedImageId;
    private int selectedImageId;
    private boolean isShowed;
    private boolean isSelected;
    private int editState;

    WidgetInfo(int nameId, int unselectedImageId, int selectedImageId, boolean isShowed, boolean isSelected, int editState) {
        this.nameId = nameId;
        this.unselectedImageId = unselectedImageId;
        this.selectedImageId = selectedImageId;
        this.isShowed = isShowed;
        this.isSelected = isSelected;
        this.editState = editState;
    }

    public static ArrayList<WidgetInfo> getAllWidgetInfo() {
        ArrayList<WidgetInfo> list = new ArrayList();
        Collections.addAll(list, values());
        return list;
    }

    public static ArrayList<WidgetInfo> getShowedWidgetInfo() {
        ArrayList<WidgetInfo> list = new ArrayList();
        for (WidgetInfo info : values()) {
            if (info.isShowed) {
                list.add(info);
            }
        }
        return list;
    }

    public static ArrayList<WidgetInfo> getHiddenWidgetInfo() {
        ArrayList<WidgetInfo> list = new ArrayList();
        for (WidgetInfo info : values()) {
            if (!info.isShowed) {
                list.add(info);
            }
        }
        return list;
    }

    public void setNameId(int nameId) {
        this.nameId = nameId;
    }

    public int getNameId() {
        return this.nameId;
    }

    public void setUnselectedImageId(int unselectedImageId) {
        this.unselectedImageId = unselectedImageId;
    }

    public int getUnselectedImageId() {
        return this.unselectedImageId;
    }

    public void setSelectedImageId(int selectedImageId) {
        this.selectedImageId = selectedImageId;
    }

    public int getSelectedImageId() {
        return this.selectedImageId;
    }

    public void setShowState(boolean isShowed) {
        this.isShowed = isShowed;
    }

    public boolean getShowState() {
        return this.isShowed;
    }

    public void setSelectState(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean getSelectState() {
        return this.isSelected;
    }

    public void setEditState(int editState) {
        this.editState = editState;
    }

    public int getEditState() {
        return this.editState;
    }
}
