import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

from sklearn.cluster import KMeans
from sklearn.metrics import silhouette_score
from sklearn.preprocessing import StandardScaler
import requests

# Fetch commit data from the API
url = "http://localhost:8080/api/commits/all"
response = requests.get(url)
if response.status_code == 200:
    data = response.json() 
    df = pd.DataFrame(data)  
else:
    print("Error:", response.status_code)
#read data from JSON file
df.head()
# Preprocess data
df["commitDate"] = pd.to_datetime(df["commitDate"])
df = df.drop_duplicates(subset=["message"])
# Vectorization
from sklearn.feature_extraction.text import TfidfVectorizer

vectorizer = TfidfVectorizer(stop_words="english")
X = vectorizer.fit_transform(df["message"])

# Apply elbow method and silhouette score to find optimal k
from sklearn.cluster import KMeans
from sklearn.metrics import silhouette_score
import matplotlib.pyplot as plt

max_k = 12
inertia = []
silhouette_scores = []
k_values = range(2, max_k + 1)

for k in k_values:
    kmeans = KMeans(n_clusters=k, random_state=42, max_iter=1000)
    cluster_labels = kmeans.fit_predict(X)   # استخدم X مش df
    
    sil_score = silhouette_score(X, cluster_labels)
    silhouette_scores.append(sil_score)
    inertia.append(kmeans.inertia_)

# Visualization
plt.figure(figsize=(14, 6))

plt.subplot(1, 2, 1)
plt.plot(k_values, inertia, marker='o')
plt.title('KMeans Inertia (Elbow Method)')
plt.xlabel('Number of Clusters (k)')
plt.ylabel('Inertia')
plt.xticks(k_values)
plt.grid(True)

plt.subplot(1, 2, 2)
plt.plot(k_values, silhouette_scores, marker='o', color='orange')
plt.title('Silhouette Scores')
plt.xlabel('Number of Clusters (k)')
plt.ylabel('Silhouette Score')
plt.xticks(k_values)
plt.grid(True)

plt.tight_layout()
plt.show()

# From the plots, choose k=5
kmeans = KMeans(n_clusters=5, random_state=42)
df["cluster"] = kmeans.fit_predict(X)
stories = {}
for _, row in df.iterrows():
    cid = row["cluster"]
    if cid not in stories:
        stories[cid] = []
    stories[cid].append(row["message"])

# Generate titles for each cluster
from sklearn.feature_extraction.text import TfidfVectorizer
import numpy as np

def get_cluster_title(messages):
    vectorizer = TfidfVectorizer(stop_words="english")
    X = vectorizer.fit_transform(messages)
    scores = np.asarray(X.sum(axis=0)).flatten()
    terms = vectorizer.get_feature_names_out()
    top_word = terms[scores.argmax()]
    return top_word.capitalize()

story_objects = []
for cid, msgs in stories.items():
    title = get_cluster_title(msgs)
    story_objects.append({
        "storyId": cid,
        "title": title,
        "commits": msgs
    })

# Save stories to JSON file
import json
with open("stories.json", "w", encoding="utf-8") as f:
    json.dump(story_objects, f, indent=2, ensure_ascii=False)

# Create FastAPI app to serve stories
from fastapi import FastAPI
import json

app = FastAPI()

@app.get("/stories")
def get_stories():
  
    with open("stories.json", "r", encoding="utf-8") as f:
        data = json.load(f)
    return data
