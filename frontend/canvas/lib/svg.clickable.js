import {Element, extend} from '@svgdotjs/svg.js';

const getCoordsFromEvent = ev => {
    if (ev.changedTouches) {
        ev = ev.changedTouches[0];
    }
    return {x: ev.clientX, y: ev.clientY};
};

// Creates handler, saves it
class ClickHandler {
    constructor(el) {
        el.remember('_clickable', this);
        this.el = el;

        this.click = this.click.bind(this);
    }

    // Enables or disabled click based on input
    init(enabled) {
        if (enabled) {
            this.el.on('mousedown.click', this.click);
            this.el.on('touchstart.click', this.click);
        } else {
            this.el.off('mousedown.click');
            this.el.off('touchstart.click');
        }
    }

    // do clicking
    click(ev) {
        const isMouse = !ev.type.indexOf('mouse');

        // Check for left button
        if (isMouse && (ev.which || ev.buttons) !== 1) {
            return;
        }

        // Prevent browser click behavior as soon as possible
        ev.preventDefault();

        // Prevent propagation to a parent that might also have clicking enabled
        ev.stopPropagation();

        // Make sure that start events are unbound so that one element
        // is only clicked by one input only
        this.init(false);

        this.box = this.el.bbox();
        this.lastClick = this.el.point(getCoordsFromEvent(ev));

        // We consider the click done, when a touch is canceled, too
        //const eventMousedown = (isMouse ? 'mousedown' : 'touchstart') + '.click';

        // Bind click event to window
        //on(window, eventMousedown, this.click);

        //this.el.dispatch('clicked', {event: ev, handler: this, box: this.box}).defaultPrevented;

        // Fire click event
        this.el.fire('clicked', {event: ev, handler: this, box: this.box, coords: this.lastClick});
    }
}

extend(Element, {
    clickable(enable = true) {
        const clickHandler = this.remember('_clickable') || new ClickHandler(this);
        clickHandler.init(enable);
        return this;
    }
});
