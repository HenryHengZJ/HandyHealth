import os
import sys
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.externals import joblib
import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
from pandas.io.json import json_normalize
from google.cloud import storage

# Use the application default credentials
cred = credentials.ApplicationDefault()
firebase_admin.initialize_app(cred, {
  'projectId': 'tflow-bbd2b',
})

db = firestore.client()
storage_client = storage.Client()
bucket_name='tflow-bbd2b.appspot.com'
bucket = storage_client.get_bucket(bucket_name)
model_bucket='eye_finalized_model.sav'
model_local='/tmp/eye_finalized_model_local.sav'

#select bucket file
blob = bucket.blob(model_bucket)
#download that file and name it 'local.joblib'
blob.download_to_filename(model_local)
#load that file from local file
loaded_model=joblib.load(model_local)

def to_str(var):
    return str(list(np.reshape(np.asarray(var), (1, np.size(var)))[0]))[1:-1]

def simplify_ages(df):
    df.Age = df.Age.fillna(-0.5)
    bins = (-1, 0, 5, 12, 18, 25, 35, 60, 120)
    group_names = ['Unknown', 'Baby', 'Child', 'Teenager', 'Student', 'Young Adult', 'Adult', 'Senior']
    categories = pd.cut(df.Age, bins, labels=group_names)
    df.Age = categories
    return df

def transform_features(df):
    df = simplify_ages(df)
    return df

#Normalize#########################################################

from sklearn import preprocessing

def encode_features(df_train):
    features = ['Age']
   
    for feature in features:
        le = preprocessing.LabelEncoder()
        le = le.fit(df_train[feature])
        df_train[feature] = le.transform(df_train[feature])
    return df_train

    
#Main function#########################################################

from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import make_scorer, accuracy_score
from sklearn.model_selection import GridSearchCV
from sklearn.model_selection import train_test_split
import json

def modelprediction(request):

    request_json = request.get_json()

    if request_json and 'message' in request_json:
        returnstr = '2'+ request_json['message']
        return returnstr
    
    elif request_json and 'uuid' in request_json:
        uuid = request_json['uuid']
        print('uuid = ', uuid)
        #Load the model from disk
        #loaded_model = joblib.load('eye_finalized_model.sav')
       
        #Retrive firestore data
        diagnose_ref = db.collection('diagnose').document(uuid)
        result_ref = db.collection('diagnose_result').document(uuid)
        df = pd.read_csv('train_probability_eye2.csv')
        headers = list(df)
        print('headers = ', headers)
        
        try:
            #Get data
            doc = diagnose_ref.get()
            print('doc = ', doc)
            #Convert JSON to dataframe
            outputpd = json_normalize(doc.to_dict())
            print('outputpd = ', outputpd)
            df = pd.DataFrame(data=outputpd, columns=headers)
            print('df = ', df.head(100).to_string())
            #Drop the output column
            #df = df.drop(['Disease'], axis=1)
            #Get the list of headers
            #newlist = list(df)
            #Fill all column with NaN to 0
            df[headers] = df[headers].fillna(0)
            
            #Rearrange data
            data_test = df
            data_test = transform_features(data_test)
            data_test = encode_features(data_test)
            print('data_test = ', data_test.head(100).to_string())
            #Drop the output column 
            X_all = data_test.drop(['Disease'], axis=1)
            y_all = data_test['Disease']
            
            predictions = loaded_model.predict(X_all)
         
            probability = loaded_model.predict_proba(X_all)
           
            #Make a dataframe with output and probability
            output = pd.DataFrame(data=probability, columns=loaded_model.classes_)
            print(output.head(100).to_string())
            
            #Convert Dataframe to JSON
            outputjson = output.to_json(orient='records', lines=True)
            print(outputjson)
            #Set outputjson to firestore
            #result_ref.set(outputjson)
            
            #os.remove(os.path.join('/tmp', 'eye_finalized_model_local.sav'))
            
            #Convert JSON to JSONString
            finaljsonString = json.loads(json.dumps(outputjson))
            
            return finaljsonString

        
        except google.cloud.exceptions.NotFound:
            print(u'No such document!')
            return 'error404'

