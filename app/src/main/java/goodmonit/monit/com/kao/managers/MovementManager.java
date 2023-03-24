package goodmonit.monit.com.kao.managers;

import java.util.ArrayList;

import goodmonit.monit.com.kao.devices.DeviceStatus;

public class MovementManager {
    private final int SLEEP_MOVEMENT_LEVEL_THRESHOLD = 2; // 수면시 Level 2 까지의 움직임은 무시
    private final int SLEEP_MOVEMENT_TIME_THRESHOLD_MIN = 3;  // 한번 움직이고 2분 동안 움직임이 없으면 잠으로 취급
    private final int DEEP_SLEEP_MOVEMENT_LEVEL_THRESHOLD = 1; // 깊은수면시 Level 1 까지의 움직임은 무시
    private final int DEEP_SLEEP_MOVEMENT_TIME_THRESHOLD_MIN = 5;  // 한번 움직이고 10분 동안 움직임이 없으면 깊은잠으로 취급

    private ArrayList<Integer> mConvertedMovementLevelData;
    private ArrayList<Integer> mConvertedSleepingData;
    private ArrayList<Integer> mSleepingData;
    private int mTotalConvertedSleepSec;
    private int mTotalConvertedDeepSleepSec;
    private int mTotalCountDeepSleepSec;
    private int mTotalCountSleepSec;
    private int mMaxMovementLevel;
    private int mMaxMovementLevelIndex;
    private int mMinMovementLevel;
    private int mMinMovementLevelIndex;
    private int sumMovementLevel, cntMovementLevel;
    private int mTotalMovementAvailableSec;

    public MovementManager() {
    }

    public void setMovementData(ArrayList<Integer> movementLevelList, ArrayList<Integer> sleepDataList) {
        if (movementLevelList == null || movementLevelList.size() == 0) return;
        sumMovementLevel = 0;
        cntMovementLevel = 0;
        mTotalCountSleepSec = 0;
        mTotalCountDeepSleepSec = 0;
        mTotalConvertedSleepSec = 0;
        mTotalConvertedDeepSleepSec = 0;
        mMaxMovementLevel = -999;
        mMinMovementLevel = 999;
        mMaxMovementLevelIndex = 0;
        mMinMovementLevelIndex = 0;

        mConvertedMovementLevelData = new ArrayList<>();
        int currLevel;

        // 움직임데이터&수면데이터 값 확인하면서 Converted움직임데이터 생성
        for (int i = 0; i < movementLevelList.size(); i++) {
            currLevel = movementLevelList.get(i);

            if (i < sleepDataList.size()) {

                // 사용안함(15), 연결끊김(-1)은 제외하고, 0~12 사이에 있는 움직임으로 판단
                if (currLevel >= 0 && currLevel <= 12) {
                    if (sleepDataList.get(i) == 1) { // 수면중이면 수면으로 입력
                        mConvertedMovementLevelData.add(DeviceStatus.MOVEMENT_SLEEP);

                    } else { // 수면중 아님면 해당하는 Level로 입력
                        cntMovementLevel++;
                        sumMovementLevel+= currLevel;
                        mConvertedMovementLevelData.add(currLevel);
                    }

                } else if (currLevel == DeviceStatus.MOVEMENT_DISCONNECTED) { // 연결끊김시에는 연결끊김으로 입력
                    mConvertedMovementLevelData.add(DeviceStatus.MOVEMENT_DISCONNECTED);
                } else if (currLevel == DeviceStatus.MOVEMENT_NOT_USING) {
                    mConvertedMovementLevelData.add(DeviceStatus.MOVEMENT_NOT_USING);
                } else {
                    mConvertedMovementLevelData.add(DeviceStatus.MOVEMENT_DISCONNECTED);
                }

            } else {
                mConvertedMovementLevelData.add(DeviceStatus.MOVEMENT_DISCONNECTED);
            }
        }
    }

    public void setSleepingData(ArrayList<Integer> movementLevelList, ArrayList<Integer> sleepDataList) {
        if (movementLevelList == null || movementLevelList.size() == 0) return;
        sumMovementLevel = 0;
        cntMovementLevel = 0;
        mTotalCountSleepSec = 0;
        mTotalCountDeepSleepSec = 0;
        mTotalConvertedSleepSec = 0;
        mMaxMovementLevel = -999;
        mMinMovementLevel = 999;
        mMaxMovementLevelIndex = 0;
        mMinMovementLevelIndex = 0;

        mSleepingData = new ArrayList<>();
        mConvertedSleepingData = new ArrayList<>();

        int cntContinuousNoMovement = 1;
        int prevLevel = -999;
        int currLevel;

        for (int i = 0; i < movementLevelList.size(); i++) {
            if (i >= 8642) break;
            currLevel = movementLevelList.get(i);

            // 사용안함(15), 연결끊김(-1)은 제외하고, 0~12 사이에 있는 움직임으로 판단
            if (currLevel >= 0 && currLevel <= 12) {
                cntMovementLevel++;
                sumMovementLevel+= currLevel;
            }

            // 0 <= level <= 2 을 만족하는 level을 움직임없음(noMovement로 판단)하고 움직임없음 Count를 증가
            // 이 움직임 없음은 향후 1)움직임없음(0), 2)수면(13), 3)깊은수면(14) 으로 분류됨
            // 움직임 없음 외로 4) 일반움직임이(1~12) 있음
            if ((0 <= prevLevel) && (prevLevel <= SLEEP_MOVEMENT_LEVEL_THRESHOLD)
                    && (0 <= currLevel) && (currLevel <= SLEEP_MOVEMENT_LEVEL_THRESHOLD)
                    && (i < movementLevelList.size() - 1)) {
                cntContinuousNoMovement++;
            } else {
                // 0 <= level <= 2 을 만족하지 않을 경우, 움직임없음 Count가 있는지 확인
                // 움직임없음 Count가 없으면 일반적인 움직임이 유지되는 것으로 판단하고 해당 level을 추가하기
                // 움직임없음 Count가 있으면 잠으로 판단하기
                if (cntContinuousNoMovement == 0) {
                    mSleepingData.add(currLevel); // 일반 움직임으로 분류하고 추가
                } else {
                    // 움직임없음 Count가 3분 이상 유지되었으면, 수면으로 분류하기
                    if (cntContinuousNoMovement > SLEEP_MOVEMENT_TIME_THRESHOLD_MIN * 6) { // 3분 이상 유지될때, 잠으로 분류
                        // 여태까지 움직임없음 Count만큼 수면을 추가
                        if (i < movementLevelList.size() - 1) {
                            for (int j = 0; j < cntContinuousNoMovement + 1; j++) {
                                mSleepingData.add(DeviceStatus.MOVEMENT_SLEEP);
                            }
                        } else {
                            for (int j = 0; j < cntContinuousNoMovement; j++) {
                                mSleepingData.add(DeviceStatus.MOVEMENT_SLEEP);
                            }
                        }
                        mTotalCountSleepSec += cntContinuousNoMovement * 10;
                    } else {
                        // 유지 안되는 경우 그냥 움직임 없음으로 파악
                        // 여태까지 움직임없음 Count만큼 움직임없음을 추가
                        if (i < movementLevelList.size() - 1) {
                            for (int j = 0; j < cntContinuousNoMovement + 1; j++) {
                                mSleepingData.add(DeviceStatus.MOVEMENT_NO_MOVEMENT);
                            }
                        } else {
                            for (int j = 0; j < cntContinuousNoMovement; j++) {
                                mSleepingData.add(DeviceStatus.MOVEMENT_NO_MOVEMENT);
                            }
                        }
                    }
                    cntContinuousNoMovement = 0;
                }
            }

            if (currLevel > -1) {
                if (currLevel > mMaxMovementLevel) {
                    mMaxMovementLevel = currLevel;
                    mMaxMovementLevelIndex = i;
                }
                if (currLevel < mMinMovementLevel) {
                    mMinMovementLevel = currLevel;
                    mMinMovementLevelIndex = i;
                }
            }

            prevLevel = currLevel;
        }

        cntContinuousNoMovement = 1;
        boolean continueDeepSleep = false;
        for (int i = 0; i < movementLevelList.size(); i++) {
            currLevel = movementLevelList.get(i);

            // 0 <= level <= 1 을 만족하는 level을 움직임없음(noMovement로 판단)하고 움직임없음 Count를 증가
            // 이 움직임 없음은 향후 1)수면(13), 2)깊은수면(14) 으로 분류됨
            if ((0 <= prevLevel) && (prevLevel <= DEEP_SLEEP_MOVEMENT_LEVEL_THRESHOLD)
                    && (0 <= currLevel) && (currLevel <= DEEP_SLEEP_MOVEMENT_LEVEL_THRESHOLD)
                    && (i < movementLevelList.size() - 1)) {
                cntContinuousNoMovement++;

                if (continueDeepSleep) {
                    // 깊은수면 Flag를 확인하고 이에 따라 기존 추가된 일반수면을 깊은수면으로 변경
                    mTotalCountDeepSleepSec += 10;
                    if (mSleepingData.size() > i) {
                        mSleepingData.set(i, DeviceStatus.MOVEMENT_DEEP_SLEEP);
                    }
                } else {
                    // 움직임없음 Count가 5분 이상 유지될 때, 기존 추가된 일반수면을 깊은수면으로 변경
                    if (cntContinuousNoMovement > DEEP_SLEEP_MOVEMENT_TIME_THRESHOLD_MIN * 6) { // 5분 이상 유지될때, 깊은잠으로 분류
                        for (int j = 0; j < cntContinuousNoMovement; j++) {
                            if ((mSleepingData.size() > i - j) && (i - j >= 0)) {
                                mSleepingData.set(i - j, DeviceStatus.MOVEMENT_DEEP_SLEEP);
                            }
                        }
                        mTotalCountDeepSleepSec += cntContinuousNoMovement * 10;
                        continueDeepSleep = true;
                    }
                }
            } else {
                // 깊은수면 조건이 맞지 않으면 깊은수면 Flag를 False로 변경
                cntContinuousNoMovement = 0;
                continueDeepSleep = false;
            }

            prevLevel = currLevel;
        }

        if (sleepDataList == null) return;

        for (int i = 0; i < mSleepingData.size(); i++) {
            mConvertedSleepingData.add(mSleepingData.get(i));
        }

        int originalMovementData = 0;
        for (int i = 0; i < sleepDataList.size(); i++) {
            if (i < mConvertedSleepingData.size()) {
                originalMovementData = mConvertedSleepingData.get(i);
                if (sleepDataList.get(i) == 1) {
                    mTotalConvertedSleepSec += 10;
                    if (originalMovementData == DeviceStatus.MOVEMENT_DEEP_SLEEP) {
                        mTotalConvertedDeepSleepSec += 10;
                        mConvertedSleepingData.set(i, DeviceStatus.MOVEMENT_DEEP_SLEEP);
                    } else {
                        mConvertedSleepingData.set(i, DeviceStatus.MOVEMENT_SLEEP);
                    }
                } else {
                    mConvertedSleepingData.set(i, DeviceStatus.MOVEMENT_NO_MOVEMENT);
                }
            }
        }
    }

    public ArrayList<Integer> getConvertedMovementData() {
        return mConvertedMovementLevelData;
    }

    public ArrayList<Integer> getOriginalSleepingData() {
        return mSleepingData;
    }

    public ArrayList<Integer> getConvertedSleepingData() {
        return mConvertedSleepingData;
    }

    public int getMaxMovementLevel() {
        return mMaxMovementLevel;
    }

    public int getMaxMovementLevelIndex() {
        return mMaxMovementLevelIndex;
    }

    public int getMinMovementLevel() {
        return mMinMovementLevel;
    }

    public int getMinMovementLevelIndex() {
        return mMinMovementLevelIndex;
    }

    public int getTotalDeepSleepSec() {
        return mTotalCountDeepSleepSec;
    }

    public int getTotalSleepSec() {
        return mTotalCountSleepSec;
    }

    public int getTotalConvertedSleepSec() {
        return mTotalConvertedSleepSec;
    }

    public int getTotalConvertedDeepSleepSec() {
        return mTotalConvertedDeepSleepSec;
    }

    public int getTotalMovementLevel() {
        return sumMovementLevel;
    }

    public int getTotalMovementCount() {
        return cntMovementLevel;
    }

}
