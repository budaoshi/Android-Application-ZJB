package com.idrv.coach.data.event;


import android.support.annotation.NonNull;

import com.idrv.coach.utils.ValidateUtil;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * time: 2015/8/21
 * description:Rx
 *
 * @author sunjianfei
 */
public class RxBus {
    private static final String TAG = RxBus.class.getSimpleName();
    private static RxBus instance;

    public static synchronized RxBus get() {
        if (null == instance) {
            instance = new RxBus();
        }
        return instance;
    }

    private RxBus() {
    }

    private Hashtable<String, Map<String, Subject>> mSubjects = new Hashtable<>();

    public <T> Observable<T> register(@NonNull String pageKey,
                                      @NonNull String eventKey,
                                      @NonNull Class<T> clazz) {
        Map<String, Subject> map = mSubjects.get(pageKey);
        if (null == map) {
            map = new HashMap<String, Subject>();
            mSubjects.put(pageKey, map);
        }
        Subject<T> subject = map.get(eventKey);
        if (null == subject) {
            subject = PublishSubject.create();
            map.put(eventKey, subject);
        }
        return subject;
    }

    public void unregister(@NonNull String pageKey,
                           @NonNull String eventKey) {
        Map<String, Subject> map = mSubjects.get(pageKey);
        if (ValidateUtil.isValidate(map)) {
            map.remove(eventKey);
        }
    }

    public void unregister(@NonNull String pageKey) {
        mSubjects.remove(pageKey);
    }

    public void post(@NonNull Object content) {
        post(content.getClass().getName(), content);
    }

    public void post(@NonNull final Object tag, @NonNull final Object content) {
        //Map.Entry<String, Map<Object, Subject>
        Observable.fromIterable(mSubjects.entrySet())
                .map(stringMapEntry -> stringMapEntry.getValue())
                .filter(stringSubjectMap -> ValidateUtil.isValidate(stringSubjectMap))
                .map(objectSubjectMap -> objectSubjectMap.get(tag))
                .subscribe(subject -> {
                    //防止没有注册观察者，这里空指针异常
                    if (subject != null) {
                        subject.onNext(content);
                    }

                }, throwable -> throwable.printStackTrace());
    }
}
