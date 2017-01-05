Ginny
====

## Example
1. create a github repo: `my/mac-client-repo`
2. [genreate an access token](https://github.com/settings/tokens): `my-access-token`
3. push your changelog markdown: `my-changelog.md`
5. create a bucket(`my-bucket`) in [qiniu](https://qiniu.com) and get access-key(`my-qiniu-access-key`) and secret-key(`my-qiniu-secret-key`)
6. create an incoming robot in [bearychat](https://bearychat.com) and get the hook url: `https://bearychat-hook-url`
7. setup [ginny](https://github.com/bearyinnovative/ginny) and run
8. **GET** a rendered changelog **json**: `http://your-qiniu-bucket-url/MAC-changelog.json` (**NOTE** `MAC` in `MAC-changelog.json` depends on `platform` fields in your `my-changelog.md`)
```bash
export GINNY_INCOMING_HOOK_URL="https://bearychat-hook-url"

export GINNY_CHANGELOG_CONFIG="/.../changelog-config.json"

export GINNY_QINIU_ACCESS_KEY="my-qiniu-access-key"
export GINNY_QINIU_SECRET_KEY="my-qiniu-secret-key"
export GINNY_QINIU_BUCKET="my-bucket"
```

## Usage

### Setup Envrionment
``` bash
$ cd $PROJECT
$ vim config # setup
$ source config
```

### Running
``` bash
$ cd $PROJECT
$ lein run
```

## Changelog Example

### Changelog Markdown
```markdown
----
- Name: bearychat
- Platform: Windows

----

# 2.1.0 / 2016-12-24
## Client
### 32bit
- Size: 20mb
- DownloadUrl: http://bearychat.com/32bit.exe

### 64bit
- Size: 30mb
- DownloadUrl: http://bearychat.com/64bit.exe

## Fixed
- Fixed a bug
- Fixed another bug

## Added
- Added a feature
- Added another feature
- Added third feature

# 2.0.0 / 2016-11-24
## Client
### 32bit
- Size: 50mb
- DownloadUrl: http://bearychat.com/old/32bit.exe

### 64bit
- Size: 40mb
- DownloadUrl: http://bearychat.com/old/64bit.exe

## Fixed
- Fixed a bug1

## Added
- Added a feature1
- Added another feature2
```

### Render JSON
```json
{
  "header": {
    "name": "bearychat",
    "platform": "MAC"
  },
  "body": {
    "releases": [{
      "version": "2.1.0",
      "date": "2016-12-24",
      "client": {
        "32bit": {
          "size": "20mb",
          "downloadurl": "http://bearychat.com/32bit.exe"
        },
        "64bit": {
          "size": "30mb",
          "downloadurl": "http://bearychat.com/64bit.exe"
        }
      },
      "fixed": ["Fixed a bug", "Fixed another bug"],
      "added": ["Added a feature", "Added another feature",
        "Added third feature"
      ]
    }, {
      "version": "2.0.0",
      "date": "2016-11-24",
      "client": {
        "32bit": {
          "size": "50mb",
          "downloadurl": "http://bearychat.com/old/32bit.exe"
        },
        "64bit": {
          "size": "40mb",
          "downloadurl": "http://bearychat.com/old/64bit.exe"
        }
      },
      "fixed": ["Fixed a bug1"],
      "added": ["Added a feature1", "Added another feature2"]
    }]
  }
}

```

## License

Copyright © 2016 bearyinnvative
