# Word of Life Podcast Factory

Utility to construct and update the podcast rss file from the weekly message log for Word of Life Ministries of Minnesota.

## Install

1. Clone this repository
2. Run `mvn package`

## Configure

1. `mkdir ~/.wolm`

2. Get your service ID from Google and store it in the `google.properties` file.
```
$ cat ~/.wolm/google.properties
serviceAccountId=BLAHBLAH-BLAHBLAH@developer.gserviceaccount.com
```

3. Get your AWS credentials and store them in the `~/.wolm/aws.s3.properties` file.
```
$ cat ~/.wolm/aws.s3.properties 
username=Media.Department
accessKey=AKIAIAFQXYK6SIKCS76Q
secretKey=SECRETSECRETSECRETSECRET
```

## Run

1. `cd` to project directory (the one containing this README)
2. `./generate-podcast.command`
