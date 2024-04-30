import csv
import os
import requests
import concurrent.futures
import urllib3
import json

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

API_ENDPOINT = "https://vision.qa2.creditsaison.xyz/api/v1/partner/applicant/check"
AUTHORIZATION_HEADER = "Basic QGRNMU46UEAkJFcwckQ="
USERNAME_HEADER = "partnerapikey"
COOKIE_HEADER = "JSESSIONID=13F0590A81AC7CF9D3AEB727348DC099"
OUTPUT_CSV_FILE = "output.csv"
BATCH_SIZE = 10

def send_request(pan):
    payload = {
        "loanProduct": "UNC",
        "kycs": [{"kycType": "panCard", "kycValue": pan}]
    }
    headers = {
        "Content-Type": "application/json",
        "Authorization": AUTHORIZATION_HEADER,
        "username": USERNAME_HEADER,
        "Cookie": COOKIE_HEADER
    }
    try:
        response = requests.post(API_ENDPOINT, json=payload, headers=headers, verify=False)
        if response.status_code == 200:
            status = "success"
        else:
            status = "failure"
        return status, response.text
    except Exception as e:
        print(e)
        return "failure", ""

def process_batch(batch):
    with concurrent.futures.ThreadPoolExecutor() as executor:
        results = list(executor.map(send_request, batch))
    return results

def main(csv_file_path):
    with open(csv_file_path, 'r') as file:
        reader = csv.reader(file)
        next(reader)  # Skip header
        with open(OUTPUT_CSV_FILE, 'w', newline='') as output_file:
            writer = csv.writer(output_file)
            writer.writerow(["KYCValue", "Status", "Response"])
            batch = []
            for row in reader:
                for pan in row:
                    batch.append(pan)
                    if len(batch) == BATCH_SIZE:
                        results = process_batch(batch)
                        for pan, (status, response_text) in zip(batch, results):
                            writer.writerow([pan, status, response_text])
                        batch = []
            if batch:
                results = process_batch(batch)
                for pan, (status, response_text) in zip(batch, results):
                    writer.writerow([pan, status, response_text])
    print("Output CSV file has been created successfully.")

if __name__ == "__main__":
    csv_file_path = "/Users/harshavardhan/Downloads/UNI dedupe analysis.csv"
    if not os.path.exists(csv_file_path):
        print("CSV file not found. Please provide a valid file path.")
    else:
        main(csv_file_path)