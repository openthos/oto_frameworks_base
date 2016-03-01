/*
 * Copyright (C) 2014 Tieto Poland Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tieto.extension.multiwindow;

import android.graphics.Rect;
import java.lang.Object;

public class Window extends Object {

    private Rect mFrame;
    private String mPackage;
    private int mStackId;

    public Window(Rect frame, String pkg, int stackId) {
        mFrame = frame;
        mPackage = pkg;
        mStackId = stackId;
    }

    public int getStackId() {
        return mStackId;
    }

    public Rect getFrame() {
        return mFrame;
    }

    public String getPackage() {
        return mPackage;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() != getClass()) {
            return false;
        }

        Window windowObject = (Window) o;

        if (windowObject.getStackId() == this.mStackId) {
            return true;
        } else {
            return false;
        }
    }
}
