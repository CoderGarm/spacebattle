// eagerly import theme styles so as we can override them

/*
import '@vaadin/vaadin-lumo-styles/all-imports';
const $_documentContainer = document.createElement('template');
$_documentContainer.innerHTML = `
<custom-style>
<style include='lumo-badge'>

</style>
</custom-style>


`;

document.head.appendChild($_documentContainer.content);
*/


import '@vaadin/vaadin-material-styles/all-imports';

// Import the <custom-style> element from Polymer and include
// the style sheets in the global scope
import '@polymer/polymer/lib/elements/custom-style.js';

const style = document.createElement('custom-style');
style.innerHTML = `<style include="material-color material-typography"></style>`;
document.head.appendChild(style);