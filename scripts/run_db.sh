#!/bin/zsh

docker run -e POSTGRES_PASSWORD=mutombo -e POSTGRES_USER=deepfij -e POSTGRES_DATABASE=deepfij -p 5432:5432 postgres:13-alpine -c synchronous_commit=off -c default_transaction_isolation='read uncommitted'
