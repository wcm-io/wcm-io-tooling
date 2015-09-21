// Initialize FoamTree after the whole page loads to make sure
// the element has been laid out and has non-zero dimensions.
window.addEventListener("load", function() {
  var foamtree = new CarrotSearchFoamTree({
    // Identifier of the HTML element defined above
    id: "visualization",

    rolloutDuration: 0,
    pullbackDuration: 0,
    fadeDuration: 0,
    attributionPosition: 45,
    rainbowStartColor: "hsla(206, 100%, 55%, 1)",
    rainbowEndColor: "hsla(180, 100%, 55%, 1)",    

    // Some data to visualize.
    dataObject: {
      groups: [
        { label: "WCM", weight: 6, "groups": [
          { label: "Commons", weight: 1 },
          { label: "Parsys", weight: 1 },
          { label: "Granite UI Extensions", weight: 1 },
          { label: "ExtJS UI Extensions", weight: 1 },
        ] },
        { label: "DAM", weight: 1, "groups": [
          { label: "DAM Asset Service", weight: 1 },
        ] },
        { label: "Configuration", weight: 4, "groups": [
          { label: "API", weight: 1 },
          { label: "Implementation", weight: 1 },
          { label: "Editor", weight: 1 },
        ] },
        { label: "Handler", weight: 10, "groups": [
          { label: "URL Handler", weight: 1 },
          { label: "Link Handler", weight: 1 },
          { label: "Media Handler", weight: 1 },
          { label: "RichText Handler", weight: 1 },
          { label: "Handler Commons", weight: 1 },
        ] },
        { label: "Sling", weight: 2, "groups": [
          { label: "Models Extensions", weight: 1 },
          { label: "Commons", weight: 1 },
        ] },
        { label: "Testing", weight: 3, "groups": [
          { label: "AEM Mocks", weight: 1 },
          { label: "Mock Helper", weight: 1 },
          { label: "JUnit Commons", weight: 1 },
        ] },
        { label: "Samples", weight: 2},
        { label: "Tooling", weight: 6, "groups": [
          { label: "Maven", weight: 4, "groups": [
            { label: "Global Parent", weight: 1 },
            { label: "Global Build Tools", weight: 1 },
            { label: "AEM Global Parent", weight: 1 },
            { label: "AEM Dependencies", weight: 1 },
          ] },
          { label: "Maven Plugins", weight: 4, "groups": [
            { label: "CQ Maven Plugin", weight: 1 },
            { label: "Content Package Maven Plugin", weight: 1 },
            { label: "i18n Maven Plugin", weight: 1 },
            { label: "Node.js Maven Plugin", weight: 1 },
          ] },
          { label: "Netbeans", weight: 1, "groups": [
            { label: "Sightly Extension", weight: 1 },
          ] },
          { label: "Commons", weight: 1, "groups": [
            { label: "Content Package Builder", weight: 1 },
          ] },
        ] },
      ]
    }
  });
});
