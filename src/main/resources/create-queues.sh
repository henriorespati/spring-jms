#!/bin/bash

# Default values
LOG_FILE="queue_creation.log"

# Parse named arguments
while [[ $# -gt 0 ]]; do
  case "$1" in
    --user)
      USER="$2"
      shift 2
      ;;
    --password)
      PASSWORD="$2"
      shift 2
      ;;
    --broker-urls)
      BROKER_URLS="$2"
      shift 2
      ;;
    --artemis-bin)
      ARTEMIS_BIN="$2"
      shift 2
      ;;
    --input-file)
      INPUT_FILE="$2"
      shift 2
      ;;
    --log-file)
      LOG_FILE="$2"
      shift 2
      ;;
    --help|-h)
      echo "Usage: $0 --user <USER> --password <PASSWORD> --broker-urls <URL1,URL2> --artemis-bin <PATH> --input-file <CSV_FILE> --log-file <LOG_FILE>"
      exit 0
      ;;
    *)
      echo "Unknown argument: $1"
      exit 1
      ;;
  esac
done

# Validate required arguments
if [[ -z "$USER" || -z "$PASSWORD" || -z "$BROKER_URLS" || -z "$ARTEMIS_BIN" || -z "$INPUT_FILE" ]]; then
  echo "Missing required arguments."
  echo "Run with --help to see usage."
  exit 1
fi

ARTEMIS_CMD="$ARTEMIS_BIN/artemis"

# Validate artemis CLI
if [[ ! -x "$ARTEMIS_CMD" ]]; then
  echo "Artemis CLI not found at: $ARTEMIS_CMD"
  exit 2
fi

# Validate input CSV
if [[ ! -f "$INPUT_FILE" ]]; then
  echo "Input file '$INPUT_FILE' not found!"
  exit 3
fi

# Start logging
echo "Starting queue creation on $(date)" > "$LOG_FILE"

# Extract base URLs and query string from the composite URL
if [[ "$BROKER_URLS" =~ ^\((.*)\)\?(.*)$ ]]; then
  BASE_URLS="${BASH_REMATCH[1]}"
  QUERY_STRING="${BASH_REMATCH[2]}"
else
  echo "Invalid composite broker URL format"
  exit 1
fi

# Split base URLs by comma
IFS=',' read -ra INDIVIDUAL_URLS <<< "$BASE_URLS"

# Iterate over brokers
for BASE_URL in "${INDIVIDUAL_URLS[@]}"; do
  FULL_URL="${BASE_URL}?${QUERY_STRING}"
  echo "Connecting to broker: $FULL_URL" | tee -a "$LOG_FILE"

  # Process CSV (skip header)
  tail -n +2 "$INPUT_FILE" | while IFS=',' read -r ADDRESS_NAME QUEUE_NAME; do
    [[ -z "$ADDRESS_NAME" || "$ADDRESS_NAME" == \#* ]] && continue

    echo "Creating queue '$QUEUE_NAME' on address '$ADDRESS_NAME'" | tee -a "$LOG_FILE"

    "$ARTEMIS_CMD" queue create \
      --name "$QUEUE_NAME" \
      --address "$ADDRESS_NAME" \
      --url "$FULL_URL" \
      --user "$USER" \
      --password "$PASSWORD" \
      --durable \
      --anycast \
      --auto-create-address \
      --purge-on-no-consumers \
      --silent 2>&1 | grep -v "WARNING" | grep -v '^$' >> "$LOG_FILE"

    if [[ $? -eq 0 ]]; then
        echo "Success: $QUEUE_NAME on $FULL_URL" | tee -a "$LOG_FILE"
    else
        echo "Failed: $QUEUE_NAME on $FULL_URL" | tee -a "$LOG_FILE"
    fi

  done
done

echo "Queue creation completed on $(date)" | tee -a "$LOG_FILE"
