/*
 * The ARTist Project (https://artist.cispa.saarland)
 *
 * Copyright (C) 2017 CISPA (https://cispa.saarland), Saarland University
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
 *
 */

package saarland.cispa.artist.artistgui.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Entity(tableName = "instrumented_packages")
public class Package implements Parcelable {

    @PrimaryKey
    @ColumnInfo(name = "package_name")
    @NonNull
    public String packageName;

    @ColumnInfo(name = "last_instrumentation_timestamp")
    public long lastInstrumentationTimestamp;

    @ColumnInfo(name = "keep_instrumented")
    public boolean keepInstrumented;

    // module package names seperated by ','
    // This field is only for the db
    @ColumnInfo(name = "modules")
    public String modules;

    @Ignore
    private List<String> cachedModulesList;

    // Non db fields
    public static final Comparator<Package> sComparator =
            (p1, p2) -> p1.appName.compareTo(p2.appName);

    @Ignore
    public String appName;

    @Ignore
    public int appIconId;

    // Parcelable creator
    public static final Parcelable.Creator<Package> CREATOR =
            new Parcelable.Creator<Package>() {
                public Package createFromParcel(Parcel in) {
                    return new Package(in);
                }

                public Package[] newArray(int size) {
                    return new Package[size];
                }
            };

    public Package() {
    }

    @Ignore
    public Package(@NonNull String packageName) {
        this.packageName = packageName;
    }

    @Ignore
    public Package(@NonNull String packageName, long lastInstrumentationTimestamp,
                   boolean keepInstrumented) {
        this.packageName = packageName;
        this.lastInstrumentationTimestamp = lastInstrumentationTimestamp;
        this.keepInstrumented = keepInstrumented;
    }

    @Ignore
    public Package(@NonNull String appName, @NonNull String packageName, int appIconId) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIconId = appIconId;
    }

    @Ignore
    private Package(Parcel in) {
        appName = in.readString();
        packageName = in.readString();
        appIconId = in.readInt();
        lastInstrumentationTimestamp = in.readLong();
        keepInstrumented = in.readByte() == 1;
        modules = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appName);
        dest.writeString(packageName);
        dest.writeInt(appIconId);
        dest.writeLong(lastInstrumentationTimestamp);
        dest.writeByte((byte) (keepInstrumented ? 1 : 0));
        dest.writeString(modules);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Package aPackage = (Package) o;
        return appIconId == aPackage.appIconId &&
                appName.equals(aPackage.appName) &&
                packageName.equals(aPackage.packageName);

    }

    @Override
    public int hashCode() {
        int result = appName.hashCode();
        result = 31 * result + packageName.hashCode();
        result = 31 * result + appIconId;
        return result;
    }

    /**
     * Can be called after a instrumentation removal to reset the package state.
     */
    public void reset() {
        lastInstrumentationTimestamp = 0;
        keepInstrumented = false;
    }

    private void buildCachedModuleList() {
        if (cachedModulesList == null && modules != null && !modules.isEmpty()) {
            String[] result = modules.contains(",") ?
                    modules.split(",") : new String[]{modules};
            cachedModulesList = Arrays.asList(result);
        }
    }

    public List<String> getModulesAsList() {
        buildCachedModuleList();
        return cachedModulesList;
    }

    public void addModules(String[] packageNames) {
        buildCachedModuleList();

        final List<String> currentModules = cachedModulesList;
        for (String name : packageNames) {
            if (!currentModules.contains(name)) {
                currentModules.add(name);
            }
        }
        updateModulesDBString();
    }

    public void removeModules(List<String> modules) {
        buildCachedModuleList();
        cachedModulesList.removeAll(modules);
        updateModulesDBString();
    }

    private void updateModulesDBString() {
        buildCachedModuleList();
        final List<String> cachedModules = cachedModulesList;

        final int size = cachedModules.size();
        if (size == 1) modules = cachedModules.get(0);
        else if (size > 0) {
            StringBuilder builder = new StringBuilder(cachedModules.get(0));
            // Loop starts from second value
            for (int i = 1; i < size; i++) {
                builder.append(",")
                        .append(cachedModules.get(i));
            }
            modules = builder.toString();
        }
        throw new IllegalStateException("No packageName passed to addModules()");
    }

}
