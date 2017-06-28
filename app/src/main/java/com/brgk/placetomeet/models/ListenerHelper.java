package com.brgk.placetomeet.models;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import com.brgk.placetomeet.R;
import com.brgk.placetomeet.activities.MapActivity;

public class ListenerHelper {

    private MapActivity parentActivity;
    private GestureDetector gestureDetector;

    public ListenerHelper( MapActivity parentActivity ) {
        gestureDetector = new GestureDetector(parentActivity, new SingleTapConfirm());
        this.parentActivity = parentActivity;
    }

    public void setListener(View viewElement, String type) {
        switch (type) {
            case "click":
                chooseClickListener(viewElement);
                break;
            case "touch":
                chooseTouchListener(viewElement);
                break;
            case "seekBarChange":
                chooseSeekBarChangeListener(viewElement);
                break;
        }
    }

    private void chooseSeekBarChangeListener(View viewElement){
        ((SeekBar) viewElement).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                parentActivity.radiusText.setText(progress + "m");
                if (parentActivity.centerCircle != null) {
                    parentActivity.centerCircle.setRadius(progress);
                    parentActivity.centerOfCircle.setRadius(progress/30);
                }
                parentActivity.updateList(parentActivity.places);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void chooseClickListener(View viewElement) {
        final int id = viewElement.getId();
        viewElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (id) {
                    case R.id.show_fav:
                        parentActivity.showFav();
                        break;
                    case R.id.floatingActionButton:
                        parentActivity.onFloatingButtonClick();
                        break;
                    case R.id.getMyLocationButton:
                        parentActivity.onGetMyLocationButtonClick();
                        break;
                    case R.id.rankby_button:
                        parentActivity.updateMapElements();
                        break;
                    case R.id.right_slider_add_new_person_container:
                        if(!parentActivity.isAddingPerson) {
                            parentActivity.onFloatingButtonClick();
                        }
                        break;
                }
            }
        });
    }

    private void chooseTouchListener(final View viewElement) {
        final int id = viewElement.getId();
        View.OnTouchListener rightSliderListener = new View.OnTouchListener() {
            float startX;
            boolean started = false;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float toX;
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_MOVE:
                        if (!started) {
                            startX = motionEvent.getRawX();
                            started = true;
                        }
                        if ((motionEvent.getRawX() - startX) < 0) {
                            toX = parentActivity.screenWidth - parentActivity.rightSliderWidth;
                        } else {
                            toX = parentActivity.screenWidth - parentActivity.rightSliderWidth + motionEvent.getRawX() - startX;
                        }

                        parentActivity.rightHandle.animate().x(toX - parentActivity.rightHandleWidth).setDuration(0).start();
                        parentActivity.rightSlider.animate().x(toX).setDuration(0).start();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (motionEvent.getRawX() - startX > 0.1 * parentActivity.rightSliderWidth) {
                            toX = parentActivity.screenWidth;
                            parentActivity.rightSliderOpened = false;
                        } else {
                            toX = parentActivity.screenWidth - parentActivity.rightSliderWidth;
                            parentActivity.rightSliderOpened = true;
                        }

                        started = false;

                        parentActivity.rightHandle.animate().x(toX - parentActivity.rightHandleWidth).setDuration(100).start();
                        parentActivity.rightSlider.animate().x(toX).setDuration(100).start();
                        break;
                    default:
                        return false;
                }
                return false;
            }
        };

        switch (id) {
            case R.id.left_handle:
                View.OnTouchListener leftHandleListener = new View.OnTouchListener() {
                    float x;

                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        float toX;
                        if (!parentActivity.rightSliderOpened) {
                            if (gestureDetector.onTouchEvent(motionEvent)) {
                                if (parentActivity.leftSliderOpened) {
                                    toX = parentActivity.leftHandleDefaultX;
                                    parentActivity.leftSliderOpened = false;
                                } else {
                                    toX = parentActivity.leftSliderWidth;
                                    parentActivity.leftSliderOpened = true;
                                }
                                view.animate().x(toX).setDuration(100).start();
                                parentActivity.leftSlider.animate().x(toX - parentActivity.leftSliderWidth).setDuration(100).start();
                                return true;
                            } else {
                                switch (motionEvent.getActionMasked()) {
                                    case MotionEvent.ACTION_DOWN:
                                        x = view.getX() - motionEvent.getRawX();
                                        break;
                                    case MotionEvent.ACTION_MOVE:
                                        if ((x + motionEvent.getRawX()) > parentActivity.leftSliderWidth) {
                                            toX = parentActivity.leftSliderWidth;
                                        } else {
                                            toX = x + motionEvent.getRawX();
                                        }
                                        view.animate().x(toX).setDuration(0).start();
                                        parentActivity.leftSlider.animate().x(toX - parentActivity.leftSliderWidth).setDuration(0).start();
                                        break;
                                    case MotionEvent.ACTION_UP:
                                        if (parentActivity.leftSliderOpened) {
                                            if ((x + motionEvent.getRawX()) < 0.9 * parentActivity.leftTotalWidth) {
                                                toX = parentActivity.leftHandleDefaultX;
                                                parentActivity.leftSliderOpened = false;
                                            } else {
                                                toX = parentActivity.leftSliderWidth;
                                                parentActivity.leftSliderOpened = true;
                                            }
                                        } else {
                                            if ((x + motionEvent.getRawX()) > 0.1 * parentActivity.leftTotalWidth) {
                                                toX = parentActivity.leftSliderWidth;
                                                parentActivity.leftSliderOpened = true;
                                            } else {
                                                toX = parentActivity.leftHandleDefaultX;
                                                parentActivity.leftSliderOpened = false;
                                            }
                                        }
                                        view.animate().x(toX).setDuration(100).start();
                                        parentActivity.leftSlider.animate().x(toX - parentActivity.leftSliderWidth).setDuration(100).start();
                                        break;
                                    default:
                                        return false;
                                }
                            }
                        }
                        return true;
                    }
                };
                viewElement.setOnTouchListener(leftHandleListener);
                break;
            case R.id.list_places:
                View.OnTouchListener leftSliderListener = new View.OnTouchListener() {
                    float startX;
                    boolean started = false;

                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        float toX;
                        switch (motionEvent.getActionMasked()) {
                            case MotionEvent.ACTION_MOVE:
                                if (!started) {
                                    startX = motionEvent.getRawX();
                                    started = true;
                                }
                                if ((motionEvent.getRawX() - startX) > 0) {
                                    toX = 0;
                                } else {
                                    toX = motionEvent.getRawX() - startX;
                                }

                                parentActivity.leftHandle.animate().x(toX + parentActivity.leftSliderWidth).setDuration(0).start();
                                parentActivity.leftSlider.animate().x(toX).setDuration(0).start();
                                break;
                            case MotionEvent.ACTION_UP:
                                if (motionEvent.getRawX() - startX < -0.1 * parentActivity.leftSliderWidth) {
                                    toX = -parentActivity.leftSliderWidth;
                                    parentActivity.leftSliderOpened = false;
                                } else {
                                    toX = 0;
                                    parentActivity.leftSliderOpened = true;
                                }

                                started = false;

                                parentActivity.leftHandle.animate().x(toX + parentActivity.leftSliderWidth).setDuration(100).start();
                                parentActivity.leftSlider.animate().x(toX).setDuration(100).start();
                                break;
                            default:
                                return false;
                        }
                        return false;
                    }
                };
                viewElement.setOnTouchListener(leftSliderListener);
                break;
            case R.id.right_handle:
                View.OnTouchListener rightHandleListener = new View.OnTouchListener() {
                    float x;

                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        float toX;

                        if (!parentActivity.leftSliderOpened) {
                            if (gestureDetector.onTouchEvent(motionEvent)) {
                                if (parentActivity.rightSliderOpened) {
                                    toX = parentActivity.rightHandleDefaultX;
                                    parentActivity.rightSliderOpened = false;
                                } else {
                                    toX = parentActivity.screenWidth - parentActivity.rightTotalWidth;
                                    parentActivity.rightSliderOpened = true;
                                }
                                view.animate().x(toX).setDuration(100).start();
                                parentActivity.rightSlider.animate().x(toX + parentActivity.rightHandleWidth).setDuration(100).start();
                                return true;
                            } else {
                                switch (motionEvent.getActionMasked()) {
                                    case MotionEvent.ACTION_DOWN:
                                        x = view.getX() - motionEvent.getRawX();
                                        break;
                                    case MotionEvent.ACTION_MOVE:
                                        if ((x + motionEvent.getRawX()) < (parentActivity.screenWidth - (parentActivity.rightTotalWidth))) {
                                            toX = parentActivity.screenWidth - (parentActivity.rightTotalWidth);
                                        } else {
                                            toX = x + motionEvent.getRawX();
                                        }
                                        view.animate().x(toX).setDuration(0).start();
                                        parentActivity.rightSlider.animate().x(toX + parentActivity.rightHandleWidth).setDuration(0).start();
                                        break;
                                    case MotionEvent.ACTION_UP:
                                        if (parentActivity.rightSliderOpened) {
                                            if ((x + motionEvent.getRawX()) > (parentActivity.screenWidth - 0.9 * (parentActivity.rightTotalWidth))) {
                                                toX = parentActivity.rightHandleDefaultX;
                                                parentActivity.rightSliderOpened = false;
                                            } else {
                                                toX = parentActivity.screenWidth - parentActivity.rightTotalWidth;
                                                parentActivity.rightSliderOpened = true;
                                            }
                                        } else {
                                            if ((x + motionEvent.getRawX()) < (parentActivity.screenWidth - 0.1 * (parentActivity.rightTotalWidth))) {
                                                toX = parentActivity.screenWidth - parentActivity.rightTotalWidth;
                                                parentActivity.rightSliderOpened = true;
                                            } else {
                                                toX = parentActivity.rightTotalWidth;
                                                parentActivity.rightSliderOpened = false;
                                            }
                                        }
                                        view.animate().x(toX).setDuration(100).start();
                                        parentActivity.rightSlider.animate().x(toX + parentActivity.rightHandleWidth).setDuration(100).start();
                                        break;
                                    default:
                                        return false;
                                }
                            }
                        }
                        return true;
                    }
                };
                viewElement.setOnTouchListener(rightHandleListener);
                break;
            case R.id.right_slider_persons:
                viewElement.setOnTouchListener(rightSliderListener);
                break;
            case R.id.show_fav:
                viewElement.setOnTouchListener(rightSliderListener);
                break;
            case R.id.footer:
                viewElement.setOnTouchListener(new View.OnTouchListener() {
                    float y;
                    int topOffset = parentActivity.getActionBarHeight() + parentActivity.getStatusBarHeight();

                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        float toY;
                        parentActivity.closeBothSliders();

                        if (gestureDetector.onTouchEvent(motionEvent)) {
                            if (parentActivity.footerOpened) {
                                toY = parentActivity.screenHeight - topOffset;
                                parentActivity.footerOpened = false;
                            } else {
                                toY = parentActivity.footerTop + viewElement.getHeight();
                                parentActivity.footerOpened = true;
                            }
                            parentActivity.footerSlider.animate().y(toY).setDuration(100).start();
                            view.animate().y(toY - viewElement.getHeight()).setDuration(100).start();
                            return false;
                        } else {
                            switch (motionEvent.getActionMasked()) {
                                case MotionEvent.ACTION_DOWN:
                                    y = motionEvent.getRawY() - view.getY() - view.getHeight();
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    if (motionEvent.getRawY() - y < parentActivity.footerTop + viewElement.getHeight()) {
                                        toY = parentActivity.footerTop + viewElement.getHeight();
                                    } else {
                                        toY = motionEvent.getRawY() - y;
                                    }

                                    parentActivity.footerSlider.animate().y(toY).setDuration(0).start();
                                    view.animate().y(toY - viewElement.getHeight()).setDuration(0).start();
                                    break;
                                case MotionEvent.ACTION_UP:
                                    if (parentActivity.footerOpened) {
                                        if (motionEvent.getRawY() - y > 0.05 * parentActivity.footerSlider.getHeight()) {
                                            toY = parentActivity.screenHeight - topOffset; //parentActivity.getPixelsFromDp(512);
                                            parentActivity.footerOpened = false;
                                        } else {
                                            toY = parentActivity.footerTop + viewElement.getHeight();
                                            parentActivity.footerOpened = true;
                                        }
                                    } else {
                                        if (motionEvent.getRawY() - y < 0.95 * parentActivity.footerSlider.getHeight()) {
                                            toY = parentActivity.footerTop + viewElement.getHeight();
                                            parentActivity.footerOpened = true;
                                        } else {
                                            toY = parentActivity.screenHeight - topOffset;
                                            parentActivity.footerOpened = false;
                                        }
                                    }
                                    parentActivity.footerSlider.animate().y(toY).setDuration(100).start();
                                    view.animate().y(toY - viewElement.getHeight()).setDuration(100).start();
                                    break;
                                default:
                                    return false;
                            }
                        }
                        return true;
                    }
                });
                break;
            case R.id.footer_slider:
                viewElement.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return true;
                    }
                });
                break;
            case R.id.right_container:
                viewElement.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return true;
                    }
                });
                break;
        }
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }

    }
}