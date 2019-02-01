Commands to start dev environment 

```bash
lein less auto
lein with-profile dev figwheel ui-dev
lein cljsbuild auto main-dev
lein re-frisk
# after compile of ui-dev and main-dev, 
electron .
```

We have 2 builds, ui and electron main. UI pulls from both the `/cljs/ui` and `/cljs/common` folders. The main class is `electro-note.core`. Main pulls from both the `/cljs/main` and `/cljs/common` folders. The main class is `electro-note.main-proc.core`. 

todo:

 - Create and validate `-min` builds.
 
