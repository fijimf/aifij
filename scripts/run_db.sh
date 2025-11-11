#!/bin/zsh

docker run \
  -e POSTGRES_PASSWORD=mutombo \
  -e POSTGRES_USER=deepfij \
  -e POSTGRES_DATABASE=deepfij \
  -p 5432:5432 \
  postgres:13-alpine \
  -c synchronous_commit=off \
  -c default_transaction_isolation='read uncommitted' \
  -c shared_buffers=256MB \
  -c effective_cache_size=1GB \
  -c maintenance_work_mem=128MB \
  -c work_mem=8MB \
  -c wal_buffers=8MB \
  -c checkpoint_completion_target=0.9 \
  -c max_wal_size=1GB \
  -c random_page_cost=1.1 \
  -c effective_io_concurrency=200
