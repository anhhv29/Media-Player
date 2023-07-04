package ex.media_player.bubbles;

import android.view.View;
import android.view.WindowManager;

final class BubblesLayoutCoordinator {
    private static BubblesLayoutCoordinator INSTANCE;
    private BubbleTrashLayout trashView;
    private WindowManager windowManager;
    private BubblesService bubblesService;

    private static BubblesLayoutCoordinator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BubblesLayoutCoordinator();
        }
        return INSTANCE;
    }

    private BubblesLayoutCoordinator() {
    }

    public void notifyBubblePositionChanged(BubbleLayout bubble, int x, int y) {
        if (trashView != null) {
            trashView.setVisibility(View.VISIBLE);
            if (checkIfBubbleIsOverTrash(bubble)) {
                trashView.applyMagnetism();
                trashView.vibrate();
                applyTrashMagnetismToBubble(bubble);
            } else {
                trashView.releaseMagnetism();
            }
        }
    }

    private void applyTrashMagnetismToBubble(BubbleLayout bubble) {
        View trashContentView = getTrashContent();
        int trashCenterX = (trashContentView.getLeft() + (trashContentView.getMeasuredWidth() / 2));
        int trashCenterY = (trashContentView.getTop() + (trashContentView.getMeasuredHeight() / 2));
        int x = (trashCenterX - (bubble.getMeasuredWidth() / 2));
        int y = (trashCenterY - (bubble.getMeasuredHeight() / 2));
        bubble.getViewParams().x = x;
        bubble.getViewParams().y = y;
        windowManager.updateViewLayout(bubble, bubble.getViewParams());
    }

    private boolean checkIfBubbleIsOverTrash(BubbleLayout bubble) {
        boolean result = false;
        if (trashView.getVisibility() == View.VISIBLE) {
            View trashContentView = getTrashContent();
            int trashWidth = trashContentView.getMeasuredWidth();
            int trashHeight = trashContentView.getMeasuredHeight();
            int trashLeft = (trashContentView.getLeft() - (trashWidth / 4));
            int trashRight = (trashContentView.getLeft() + trashWidth + (trashWidth / 4));
            int trashTop = (trashContentView.getTop() - (trashHeight / 4));
            int trashBottom = (trashContentView.getTop() + trashHeight + (trashHeight / 4));
            int bubbleWidth = bubble.getMeasuredWidth();
            int bubbleHeight = bubble.getMeasuredHeight();
            int bubbleLeft = bubble.getViewParams().x;
            int bubbleRight = bubbleLeft + bubbleWidth;
            int bubbleTop = bubble.getViewParams().y;
            int bubbleBottom = bubbleTop + bubbleHeight;
//            Log.e("123123123", "bubbleLeft: " + bubbleLeft + "/ trashLeft: " + trashLeft + "/ bubbleRight: " + bubbleRight + "/ trashRight: " + trashRight);
//            Log.e("123123123", "bubbleTop: " + bubbleTop + "/ trashTop: " + trashTop + "/ bubbleBottom:" + bubbleBottom + "/ trashBottom:" + trashBottom);
            if (bubbleLeft <= trashLeft && bubbleRight >= trashRight) {
                if (bubbleTop <= trashTop && bubbleBottom >= trashBottom) {
                    bubble.setScaleX(0.5f);
                    bubble.setScaleY(0.5f);
                    bubble.setTranslationY(24f);
                    result = true;
                } else {
                    resetBubble(bubble);
                }
            } else {
                resetBubble(bubble);
            }
        } else {
            resetBubble(bubble);
        }
        return result;
    }

    private void resetBubble(BubbleLayout bubble) {
        bubble.setTranslationX(0f);
        bubble.setTranslationY(0f);
        bubble.setScaleX(1f);
        bubble.setScaleY(1f);
    }

    public void notifyBubbleRelease(BubbleLayout bubble) {
        if (trashView != null) {
            if (checkIfBubbleIsOverTrash(bubble)) {
                bubblesService.removeBubble(bubble);
            }
            trashView.setVisibility(View.GONE);
        }
    }

    public static class Builder {
        private BubblesLayoutCoordinator layoutCoordinator;

        public Builder(BubblesService service) {
            layoutCoordinator = getInstance();
            layoutCoordinator.bubblesService = service;
        }

        public Builder setTrashView(BubbleTrashLayout trashView) {
            layoutCoordinator.trashView = trashView;
            return this;
        }

        public Builder setWindowManager(WindowManager windowManager) {
            layoutCoordinator.windowManager = windowManager;
            return this;
        }

        public BubblesLayoutCoordinator build() {
            return layoutCoordinator;
        }
    }

    private View getTrashContent() {
        return trashView.getChildAt(0);
    }
}
