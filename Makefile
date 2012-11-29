VERSION=$(shell cat pom.xml | grep "<version>" | head -1 | grep -P "\d+\.\d+\.\d+([a-zA-Z_\-]*)?" -o)
 
PACKAGE_NAME=senseidb-$(VERSION)

TARGETS = \
  dist/senseidb-$(VERSION).zip

all: $(TARGETS)


dist/senseidb-$(VERSION).zip:
	if [ -f "$@" ]; then \
		rm $@; \
	fi
	mkdir -p dist/$(PACKAGE_NAME)
	mkdir -p dist/$(PACKAGE_NAME)/logs
	rsync -avrh --delete-after clients/ dist/$(PACKAGE_NAME)/clients
	cp -r bin dist/$(PACKAGE_NAME)/
	cp -r resources dist/$(PACKAGE_NAME)/
#	cp -r examples dist/$(PACKAGE_NAME)/
	rsync -avrhc --delete-after sensei-core/target/lib/ dist/$(PACKAGE_NAME)/lib
	cp -r lib dist/$(PACKAGE_NAME)/lib
	cp sensei-core/target/sensei-core-$(VERSION).jar dist/$(PACKAGE_NAME)/lib/
	cp sensei-federated-broker/target/sensei-federated-broker-$(VERSION)*.jar dist/$(PACKAGE_NAME)/lib/
	cp sensei-gateways/target/sensei-gateways-$(VERSION)*.jar dist/$(PACKAGE_NAME)/lib/
	cp sensei-hadoop-indexing/target/sensei-hadoop-indexing-$(VERSION)*.jar dist/$(PACKAGE_NAME)/lib/
	cd dist && \
		zip -r $(notdir $@) $(PACKAGE_NAME)

clean:
	rm -f $(TARGETS)
	rm -rf dist

.PHONY: clean
