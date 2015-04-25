TweetSentiment
==============
Qiurui Jin  
Tianlong Li
## Overview
This is the worker logic of TweetSentiment  [**TweetSentiment**](https://github.com/MonkeyLeeT/TweetSentiment).

## Usage

#### General

Upon deployment to Amazon Elastic Beanstalk as worker tier, this will automatically starts collecting messages from Amazon SQS, sending POST requests to Alchemy API and publish sentiment results to Amazon SNS.


## Design
#### Sentiment Analysis
Alchemy API is utilized to analyze tweet sentiment by sending POST request and receiving JSON analysis results

