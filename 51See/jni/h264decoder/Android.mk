# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_WHOLE_STATIC_LIBRARIES := $(LOCAL_PATH)/lib/libavformat $(LOCAL_PATH)/lib/libavcodec $(LOCAL_PATH)/lib/libavutil $(LOCAL_PATH)/lib/libavfilter $(LOCAL_PATH)/lib/libswscale $(LOCAL_PATH)/lib/libswresample

LOCAL_C_INCLUDES += $(LOCAL_PATH)/lib
LOCAL_LDLIBS += $(LOCAL_PATH)/lib/libavformat.a $(LOCAL_PATH)/lib/libavcodec.a $(LOCAL_PATH)/lib/libavfilter.a $(LOCAL_PATH)/lib/libswscale.a $(LOCAL_PATH)/lib/libavutil.a $(LOCAL_PATH)/lib/libswresample.a

LOCAL_LDLIBS    += -llog

LOCAL_MODULE    := Arm7VideoDecoder
LOCAL_SRC_FILES := mp4.c h264.c jni.c onLoad.cpp

include $(BUILD_SHARED_LIBRARY)
