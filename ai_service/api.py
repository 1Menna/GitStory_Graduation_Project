# fastapi_stories.py

import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.cluster import KMeans
from sklearn.metrics import silhouette_score
import requests
import json
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

# ---------------------------
# FastAPI setup with CORS
# ---------------------------
app = FastAPI()

origins = [
    "http://localhost:4200",
    "http://127.0.0.1:4200"
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,        # Angular frontend
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"]
)

# ---------------------------
# Fetch commits from backend
# ---------------------------
def fetch_commits():
    url = "http://localhost:8080/api/commits/all"  # Java backend
    response = requests.get(url)
    if response.status_code == 200:
        data = response.json()
        df = pd.DataFrame(data)
        return df
    else:
        print("Error fetching commits:", response.status_code)
        return pd.DataFrame()

# ---------------------------
# Preprocess data
# ---------------------------
def preprocess(df: pd.DataFrame):
    if df.empty:
        return df
    df["commitDate"] = pd.to_datetime(df["commitDate"])
    df = df.drop_duplicates(subset=["message"])
    return df

# ---------------------------
# Cluster commits into stories
# ---------------------------
def cluster_commits(df: pd.DataFrame, k: int = 5):
    if df.empty:
        return []

    vectorizer = TfidfVectorizer(stop_words="english")
    X = vectorizer.fit_transform(df["message"])

    kmeans = KMeans(n_clusters=k, random_state=42, max_iter=1000)
    df["cluster"] = kmeans.fit_predict(X)

    stories_dict = {}
    for _, row in df.iterrows():
        cid = row["cluster"]
        if cid not in stories_dict:
            stories_dict[cid] = []
        stories_dict[cid].append(row["message"])

    # Generate story titles
    def get_cluster_title(messages):
        vec = TfidfVectorizer(stop_words="english")
        X = vec.fit_transform(messages)
        scores = np.asarray(X.sum(axis=0)).flatten()
        terms = vec.get_feature_names_out()
        top_word = terms[scores.argmax()]
        return top_word.capitalize()

    story_objects = []
    for cid, msgs in stories_dict.items():
        title = get_cluster_title(msgs)
        story_objects.append({
            "storyId": cid,
            "title": title,
            "commits": msgs
        })

    # Save to JSON
    with open("stories.json", "w", encoding="utf-8") as f:
        json.dump(story_objects, f, indent=2, ensure_ascii=False)

    return story_objects

# ---------------------------
# API endpoint
# ---------------------------
@app.get("/stories")
def get_stories():
    # Reload commits & cluster every request (or you can schedule periodically)
    df = fetch_commits()
    df = preprocess(df)
    stories = cluster_commits(df, k=5)
    return stories

# ---------------------------
# Run with:
# py -m uvicorn fastapi_stories:app --reload --port 8001
# ---------------------------
