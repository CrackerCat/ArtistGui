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

package saarland.cispa.artist.artistgui.settings.db;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

public class AddInstrumentedPackageToDbAsyncTask extends AsyncTask<String, Void, Void> {

    private Context mContext;

    public AddInstrumentedPackageToDbAsyncTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(@NonNull String... params) {
        InstrumentedPackagesManager manager = new DatabaseManager(mContext);
        for (String p : params) {
            manager.addInstrumentedPackage(p);
        }
        manager.onDestroy();
        return null;
    }
}