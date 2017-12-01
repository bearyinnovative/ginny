(ns ginny.env
  (:require [environ.core :refer [env]]))

(defn ginny-env-key
  [k]
  (keyword (format "ginny-%s" (name k))))

(defn ->int
  [k & args]
  (apply env (ginny-env-key k) args))

(defn ->str
  [k & args]
  (apply env (ginny-env-key k) args))

(defn ->bool
  [k & args]
  (Boolean/valueOf (apply env (ginny-env-key k) args)))

(defn ->keyword
  [k & args]
  (keyword (apply env (ginny-env-key k) args)))

(defn ->double
  [k & args]
  (Double/valueOf (apply env (ginny-env-key k) args)))
