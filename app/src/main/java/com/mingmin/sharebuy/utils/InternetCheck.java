package com.mingmin.sharebuy.utils;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class InternetCheck extends AsyncTask<Void, Void, InternetCheck.Result> {
    public class Result {
        public boolean hasInternet;
        public IOException ioException;

        private Result(boolean hasInternet, @Nullable IOException ioException) {
            this.hasInternet = hasInternet;
            this.ioException = ioException;
        }
    }

    private Consumer mConsumer;

    public interface Consumer {
        void accept(Result result);
    }

    public InternetCheck(Consumer consumer) {
        mConsumer = consumer;
        execute();
    }

    @Override
    protected Result doInBackground(Void... voids) {
        try {
            Socket sock = new Socket();
            sock.connect(new InetSocketAddress("8.8.8.8", 53), 1500);
            sock.close();
            return new Result(true, null);
        } catch (IOException e) {
            return new Result(false, e);
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        mConsumer.accept(result);
    }
}
