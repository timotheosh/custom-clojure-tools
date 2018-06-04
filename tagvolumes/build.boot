(def project 'tagvolumes)
(def version "0.1.0-SNAPSHOT")

(set-env! :resource-paths #{"src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "1.8.0"]
                            [adzerk/boot-test "RELEASE" :scope "test"]
                            [amazonica "0.3.127"]])


(defn usage
  []
  (println "Usage: boot run -a <environment>"))

(deftask run
  "Run the project."
  [a args ARG [str] "the arguments for the application."]
  (if (< (count args) 1)
    (usage)
    (with-pass-thru fs
      (require '[tagvolumes.core :as app])
      (apply (resolve 'app/-main) args))))

(require '[adzerk.boot-test :refer [test]])
