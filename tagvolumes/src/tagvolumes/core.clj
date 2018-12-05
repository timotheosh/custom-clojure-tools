(ns tagvolumes.core
  (:require [amazonica.aws.ec2 :as ec2])
  (:gen-class))

(def regions ["us-east-1"
              "ap-southeast-2" "ap-northeast-1"
              "eu-west-1" "eu-central-1"])

(def envs {:dev "inindca.com" :test "inintca.com" :prod "mypurecloud.com"
           :prod-apse2 "mypurecloud.com.au" :prod-apne1 "mypurecloud.jp"
           :prod-euw1 "mypurecloud.ie" :prod-euc1 "mypurecloud.de"})

(defn creds
  [env]
  (if (= env "prod")
    (vec
     (for [region regions]
       {:profile env
        :endpoint region}))
    [{:profile env
      :endpoint "us-east-1"}]))

(defn get-toku-filter
  []
  (let [cluster-names ["bridgedb" "directorydb" "realtimedb"]
        cluster-number 3]
    (vec
     (flatten
      (for [x cluster-names]
        (for [y (range 1 (inc cluster-number))]
          (str x "-rs1-" y)))))))

(defn get-cluster-filter
  "Returns a flat vector suitable for an AWS EC2 filter.
    cluster-names must be a sequence,
    cluster size must be equal to the number of nodes in a cluster
      and assumes a sequential integer beginning with 1."
  [cluster-names cluster-size]
  (vec
   (flatten
    (for [x cluster-names]
      (for [y (range 1 (inc cluster-size))]
        (str x "-" y))))))

(defn- aws-find-volumes
  [credentials filter]
  (second
   (first
    (ec2/describe-volumes
     credentials
     :filters [{:name "tag:Name" :values filter}]))))

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
  [cred filter]
  (let [volumes (aws-find-volumes cred filter)]
    (doseq [vol (find-volumes volumes)]
      (let [volume-id (:volume-id vol)
            key-name (:key-name vol)]
        (println "Adding tag volume-id: " volume-id
                 "\tkey-name: " key-name)
        (ec2/create-tags
         cred
         {:resources [volume-id]
          :tags [{:key "key-name"
                  :value key-name}]})))))

(defn -main
  "Entry point for boot script. Runs create-tags."
  [& args]
  (let [env (first args)]
    (doseq [cred (creds env)]
      (create-tags cred))))
