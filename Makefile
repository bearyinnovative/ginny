profile = "package"
build_date = `date +%Y%m%d%H%M`
commit = `git rev-parse HEAD`
version = `git rev-parse --short HEAD`
package-image = hub.didiyun.com/bearyinnovative/ginny-package:$(version)
release-image = hub.didiyun.com/bearyinnovative/ginny:$(version)
maven-repo-dir = /data/maven2/repository

# Common commands

## 编译及构建
.PHONY: release
release:
	mkdir -p output/
	lein with-profile $(profile) run build -o output/

## 清理当前工作目录
.PHONY: clean
clean:
	git clean -fdx

# Commands used to generate docker image

## 构建 package 镜像
.PHONY: build-package-image
build-package-image:
	docker build . \
		--no-cache \
		--force-rm \
		--build-arg build_date=$(build_date) \
		--build-arg version=$(version) \
		--build-arg commit=$(commit) \
		-t $(package-image) \
		-f package.Dockerfile

## 构建 release 镜像
.PHONY: build-release-image
build-release-image:
	docker build . \
		--no-cache \
		--force-rm \
		--build-arg build_date=$(build_date) \
		--build-arg version=$(version) \
		--build-arg commit=$(commit) \
		-t $(release-image) \
		-f release.Dockerfile

# Commands used to run with clean docker container

## 使用 package 镜像构建项目
.PHONY: package
package:
	docker run -u`id -u`:`id -g` \
		--rm \
		-v $(maven-repo-dir):/tmp/.m2/repository \
		-v $(CURDIR):/workspace $(package-image)
