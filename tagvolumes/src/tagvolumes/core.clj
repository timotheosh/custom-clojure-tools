(ns tagvolumes.core
  (:require [amazonica.aws.ec2 :as ec2])
  (:gen-class))

(def creds {:profile "dev"
            :region "us-east-1"})

(defn get-toku-filter
  []
  (let [cluster-names ["bridgedb" "directorydb" "realtimedb"]
        cluster-number 3]
    (vec
     (flatten
      (for [x cluster-names]
        (for [y (range 1 (inc cluster-number))]
          (str x "-rs1-" y)))))))

(defn- aws-find-volumes
  [credentials]
  (second
   (first
    (ec2/describe-volumes
     credentials
     :filters [{:name "tag:Name" :values (get-toku-filter)}]))))

(defn find-volumes
  [vol-list]
  (for [x vol-list]
    {:volume-id (:volume-id x) :key-name
     (:value
      (first
       (filter
        #(= (:key %) "Name")
        (:tags x))))}))

(defn create-tags
  []
  (println "I am here...")
  (let [volumes (aws-find-volumes creds)]
    (doseq [vol (find-volumes volumes)]
      (let [volume-id (:volume-id vol)
            key-name (:key-name vol)]
        (println "Adding tag volume-id: " volume-id
                 "\tkey-name: " key-name)
        (ec2/create-tags {:resources [volume-id]
                          :tags [{:key "key-name"
                                  :value key-name}]})))))

(defn -main
  "Entry point for boot script. Runs create-tags."
  [& args]
  (create-tags))
