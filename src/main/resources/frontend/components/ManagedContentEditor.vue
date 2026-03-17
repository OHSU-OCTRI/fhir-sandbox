<template>
  <div class="editor">
    <div class="menubar">
      <div v-if="editor" class="toolbar">
        <button
          class="menubar__button"
          type="button"
          title="Bold"
          :class="{ 'is-active': editor.isActive('bold') }"
          @click="editor.chain().focus().toggleBold().run()"
        >
          <i class="fas fa-bold"></i>
        </button>

        <button
          class="menubar__button"
          type="button"
          title="Italic"
          :class="{ 'is-active': editor.isActive('italic') }"
          @click="editor.chain().focus().toggleItalic().run()"
        >
          <i class="fas fa-italic"></i>
        </button>

        <button
          class="menubar__button"
          type="button"
          title="Strikethrough"
          :class="{ 'is-active': editor.isActive('strike') }"
          @click="editor.chain().focus().toggleStrike().run()"
        >
          <i class="fas fa-strikethrough"></i>
        </button>
        <button
          class="menubar__button"
          type="button"
          title="Heading 1"
          :class="{ 'is-active': editor.isActive('heading', { level: 1 }) }"
          @click="editor.chain().focus().toggleHeading({ level: 1 }).run()"
        >
          <span class="menu-text-icon">H1</span>
        </button>
        <button
          class="menubar__button"
          type="button"
          title="Heading 2"
          :class="{ 'is-active': editor.isActive('heading', { level: 2 }) }"
          @click="editor.chain().focus().toggleHeading({ level: 2 }).run()"
        >
          <span class="menu-text-icon">H2</span>
        </button>
        <button
          class="menubar__button"
          type="button"
          title="Heading 3"
          :class="{ 'is-active': editor.isActive('heading', { level: 3 }) }"
          @click="editor.chain().focus().toggleHeading({ level: 3 }).run()"
        >
          <span class="menu-text-icon">H3</span>
        </button>

        <label for="create-color">
          <i class="fas fa-palette"></i>
          <input
            id="create-color"
            type="color"
            style="display: none"
            @input="editor.chain().focus().setColor($event.target.value).run()"
            :value="editor.getAttributes('textStyle').color"
          />
        </label>

        <button
          class="menubar__button"
          type="button"
          title="Left Align"
          @click="editor.chain().focus().setTextAlign('left').run()"
          :class="{ 'is-active': editor.isActive({ textAlign: 'left' }) }"
        >
          <i class="fa-solid fa-align-left"></i>
        </button>
        <button
          class="menubar__button"
          type="button"
          title="Center"
          @click="editor.chain().focus().setTextAlign('center').run()"
          :class="{ 'is-active': editor.isActive({ textAlign: 'center' }) }"
        >
          <i class="fa-solid fa-align-center"></i>
        </button>
        <button
          class="menubar__button"
          type="button"
          title="Right Align"
          @click="editor.chain().focus().setTextAlign('right').run()"
          :class="{ 'is-active': editor.isActive({ textAlign: 'right' }) }"
        >
          <i class="fa-solid fa-align-right"></i>
        </button>

        <button
          class="menubar__button"
          type="button"
          title="Bullet list"
          :class="{ 'is-active': editor.isActive('bulletList') }"
          @click="editor.chain().focus().toggleBulletList().run()"
        >
          <i class="fas fa-list-ul"></i>
        </button>

        <button
          class="menubar__button"
          type="button"
          title="Ordered list"
          :class="{ 'is-active': editor.isActive('orderedList') }"
          @click="editor.chain().focus().toggleOrderedList().run()"
        >
          <i class="fas fa-list-ol"></i>
        </button>

        <button
          class="menubar__button"
          type="button"
          title="Block quote"
          :class="{ 'is-active': editor.isActive('blockquote') }"
          @click="editor.chain().focus().toggleBlockquote().run()"
        >
          <i class="fas fa-quote-left"></i>
        </button>

        <button
          class="menubar__button"
          type="button"
          title="Link"
          @click="setLink"
          :class="{ 'is-active': editor.isActive('link') }"
        >
          <i class="fa fa-link"></i>
        </button>
        <button class="menubar__button" type="button" title="image" @click="addImage">
          <i class="far fa-image"></i>
        </button>
        <!-- Table controls -->
        <!-- Icons are from: https://github.com/ueberdosis/tiptap/tree/main/examples/assets/images/icons -->
        <button
          class="menubar__button"
          type="button"
          title="New Table"
          @click="
            editor
              .chain()
              .focus()
              .insertTable({ rows: 3, cols: 3, withHeaderRow: true })
              .run()
          "
        >
          <span class="svg-icon">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              :width="iconSize"
              :height="iconSize"
              viewBox="0 0 24 24"
            >
              <path
                fill-rule="evenodd"
                d="M17,17 L17,22 L19,22 C20.6568542,22 22,20.6568542 22,19 L22,17 L17,17 Z M15,17 L9,17 L9,22 L15,22 L15,17 Z M17,15 L22,15 L22,9 L17,9 L17,15 Z M15,15 L15,9 L9,9 L9,15 L15,15 Z M17,7 L22,7 L22,5 C22,3.34314575 20.6568542,2 19,2 L17,2 L17,7 Z M15,7 L15,2 L9,2 L9,7 L15,7 Z M24,16.1768671 L24,19 C24,21.7614237 21.7614237,24 19,24 L5,24 C2.23857625,24 2.11453371e-15,21.7614237 1.77635684e-15,19 L0,5 C-3.38176876e-16,2.23857625 2.23857625,2.28362215e-15 5,0 L19,0 C21.7614237,-5.07265313e-16 24,2.23857625 24,5 L24,7.82313285 C24.0122947,7.88054124 24.0187107,7.93964623 24.0187107,8 C24.0187107,8.06035377 24.0122947,8.11945876 24,8.17686715 L24,15.8231329 C24.0122947,15.8805412 24.0187107,15.9396462 24.0187107,16 C24.0187107,16.0603538 24.0122947,16.1194588 24,16.1768671 Z M7,2 L5,2 C3.34314575,2 2,3.34314575 2,5 L2,7 L7,7 L7,2 Z M2,9 L2,15 L7,15 L7,9 L2,9 Z M2,17 L2,19 C2,20.6568542 3.34314575,22 5,22 L7,22 L7,17 L2,17 Z"
              />
            </svg>
          </span>
        </button>
        <span v-if="editor.isActive('table')">
          <button
            class="menubar__button"
            type="button"
            title="Delete Table"
            @click="editor.chain().focus().deleteTable().run()"
          >
            <span class="svg-icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                :width="iconSize"
                :height="iconSize"
                viewBox="0 0 24 24"
              >
                <path
                  d="M19,14 C21.7600532,14.0033061 23.9966939,16.2399468 24,19 C24,21.7614237 21.7614237,24 19,24 C16.2385763,24 14,21.7614237 14,19 C14,16.2385763 16.2385763,14 19,14 Z M16.5,19.9375 L21.5,19.9375 C22.017767,19.9375 22.4375,19.517767 22.4375,19 C22.4375,18.482233 22.017767,18.0625 21.5,18.0625 L16.5,18.0625 C15.982233,18.0625 15.5625,18.482233 15.5625,19 C15.5625,19.517767 15.982233,19.9375 16.5,19.9375 Z M12.2898787,17 L9,17 L9,22 L12.6736312,22 C13.0297295,22.7496048 13.515133,23.4258795 14.1010173,24 L5,24 C2.23857625,24 -1.43817996e-15,21.7614237 -1.77635684e-15,19 L-3.55271368e-15,5 C-3.89089055e-15,2.23857625 2.23857625,5.07265313e-16 5,-1.77635684e-15 L19,-1.77635684e-15 C21.7614237,-2.28362215e-15 24,2.23857625 24,5 L24,7.82313285 C24.0122947,7.88054124 24.0187107,7.93964623 24.0187107,8 C24.0187107,8.06035377 24.0122947,8.11945876 24,8.17686715 L24,14.1010173 C23.4258795,13.515133 22.7496048,13.0297295 22,12.6736312 L22,9 L17,9 L17,12.2898787 C16.2775651,12.5048858 15.6040072,12.8333806 15,13.2546893 L15,9 L9,9 L9,15 L13.2546893,15 C12.8333806,15.6040072 12.5048858,16.2775651 12.2898787,17 Z M17,7 L22,7 L22,5 C22,3.34314575 20.6568542,2 19,2 L17,2 L17,7 Z M15,7 L15,2 L9,2 L9,7 L15,7 Z M7,2 L5,2 C3.34314575,2 2,3.34314575 2,5 L2,7 L7,7 L7,2 Z M2,9 L2,15 L7,15 L7,9 L2,9 Z M2,17 L2,19 C2,20.6568542 3.34314575,22 5,22 L7,22 L7,17 L2,17 Z"
                />
              </svg>
            </span>
          </button>

          <button
            class="menubar__button"
            type="button"
            title="Add Column Before"
            @click="editor.chain().focus().addColumnBefore().run()"
          >
            <span class="svg-icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                :width="iconSize"
                :height="iconSize"
                viewBox="0 0 24 24"
              >
                <path
                  d="M19,14 C21.7600532,14.0033061 23.9966939,16.2399468 24,19 C24,21.7614237 21.7614237,24 19,24 C16.2385763,24 14,21.7614237 14,19 C14,16.2385763 16.2385763,14 19,14 Z M21.5,19.9375 C22.017767,19.9375 22.4375,19.517767 22.4375,19 C22.4375,18.482233 22.017767,18.0625 21.5,18.0625 L20.25,18.0625 C20.077411,18.0625 19.9375,17.922589 19.9375,17.75 L19.9375,16.5 C19.9375,15.982233 19.517767,15.5625 19,15.5625 C18.482233,15.5625 18.0625,15.982233 18.0625,16.5 L18.0625,17.75 C18.0625,17.922589 17.922589,18.0625 17.75,18.0625 L16.5,18.0625 C15.982233,18.0625 15.5625,18.482233 15.5625,19 C15.5625,19.517767 15.982233,19.9375 16.5,19.9375 L17.75,19.9375 C17.922589,19.9375 18.0625,20.077411 18.0625,20.25 L18.0625,21.5 C18.0625,22.017767 18.482233,22.4375 19,22.4375 C19.517767,22.4375 19.9375,22.017767 19.9375,21.5 L19.9375,20.25 C19.9375,20.077411 20.077411,19.9375 20.25,19.9375 L21.5,19.9375 Z M2,19 C2,20.6568542 3.34314575,22 5,22 C6.65685425,22 8,20.6568542 8,19 L8,5 C8,3.34314575 6.65685425,2 5,2 C3.34314575,2 2,3.34314575 2,5 L2,19 Z M-2.7585502e-16,19 L5.81397739e-16,5 C-1.37692243e-16,2.23857625 2.23857625,0 5,0 C7.76142375,0 10,2.23857625 10,5 L10,19 C10,21.7614237 7.76142375,24 5,24 C2.23857625,24 4.43234962e-16,21.7614237 -2.7585502e-16,19 Z"
                />
              </svg>
            </span>
          </button>

          <button
            class="menubar__button"
            type="button"
            title="Add Column After"
            @click="editor.chain().focus().addColumnAfter().run()"
          >
            <span class="svg-icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                :width="iconSize"
                :height="iconSize"
                viewBox="0 0 24 24"
              >
                <path
                  d="M5,14 C7.76005315,14.0033061 9.99669388,16.2399468 10,19 C10,21.7614237 7.76142375,24 5,24 C2.23857625,24 1.77635684e-15,21.7614237 1.77635684e-15,19 C1.77635684e-15,16.2385763 2.23857625,14 5,14 Z M7.5,19.9375 C8.01776695,19.9375 8.4375,19.517767 8.4375,19 C8.4375,18.482233 8.01776695,18.0625 7.5,18.0625 L6.25,18.0625 C6.07741102,18.0625 5.9375,17.922589 5.9375,17.75 L5.9375,16.5 C5.9375,15.982233 5.51776695,15.5625 5,15.5625 C4.48223305,15.5625 4.0625,15.982233 4.0625,16.5 L4.0625,17.75 C4.0625,17.922589 3.92258898,18.0625 3.75,18.0625 L2.5,18.0625 C1.98223305,18.0625 1.5625,18.482233 1.5625,19 C1.5625,19.517767 1.98223305,19.9375 2.5,19.9375 L3.75,19.9375 C3.92258898,19.9375 4.0625,20.077411 4.0625,20.25 L4.0625,21.5 C4.0625,22.017767 4.48223305,22.4375 5,22.4375 C5.51776695,22.4375 5.9375,22.017767 5.9375,21.5 L5.9375,20.25 C5.9375,20.077411 6.07741102,19.9375 6.25,19.9375 L7.5,19.9375 Z M16,19 C16,20.6568542 17.3431458,22 19,22 C20.6568542,22 22,20.6568542 22,19 L22,5 C22,3.34314575 20.6568542,2 19,2 C17.3431458,2 16,3.34314575 16,5 L16,19 Z M14,19 L14,5 C14,2.23857625 16.2385763,0 19,0 C21.7614237,0 24,2.23857625 24,5 L24,19 C24,21.7614237 21.7614237,24 19,24 C16.2385763,24 14,21.7614237 14,19 Z"
                />
              </svg>
            </span>
          </button>
          <button
            class="menubar__button"
            type="button"
            title="Delete Column"
            @click="editor.chain().focus().deleteColumn().run()"
          >
            <span class="svg-icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                :width="iconSize"
                :height="iconSize"
                viewBox="0 0 24 24"
              >
                <path
                  d="M12.6414391,21.9312708 C12.9358807,22.5689168 13.3234155,23.1547532 13.7866134,23.6713497 C13.2317936,23.8836754 12.6294813,24 12,24 C9.23857625,24 7,21.7614237 7,19 L7,5 C7,2.23857625 9.23857625,0 12,0 C14.7614237,0 17,2.23857625 17,5 L17,12.2898787 C16.2775651,12.5048858 15.6040072,12.8333806 15,13.2546893 L15,5 C15,3.34314575 13.6568542,2 12,2 C10.3431458,2 9,3.34314575 9,5 L9,19 C9,20.6568542 10.3431458,22 12,22 C12.220157,22 12.4347751,21.9762852 12.6414391,21.9312708 Z M19,14 C21.7600532,14.0033061 23.9966939,16.2399468 24,19 C24,21.7614237 21.7614237,24 19,24 C16.2385763,24 14,21.7614237 14,19 C14,16.2385763 16.2385763,14 19,14 Z M16.5,19.9375 L21.5,19.9375 C22.017767,19.9375 22.4375,19.517767 22.4375,19 C22.4375,18.482233 22.017767,18.0625 21.5,18.0625 L16.5,18.0625 C15.982233,18.0625 15.5625,18.482233 15.5625,19 C15.5625,19.517767 15.982233,19.9375 16.5,19.9375 Z"
                />
              </svg>
            </span>
          </button>
          <button
            class="menubar__button"
            type="button"
            title="Add Row Before"
            @click="editor.chain().focus().addRowBefore().run()"
          >
            <span class="svg-icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                :width="iconSize"
                :height="iconSize"
                viewBox="0 0 24 24"
              >
                <path
                  d="M19,14 C21.7600532,14.0033061 23.9966939,16.2399468 24,19 C24,21.7614237 21.7614237,24 19,24 C16.2385763,24 14,21.7614237 14,19 C14,16.2385763 16.2385763,14 19,14 Z M21.5,19.9375 C22.017767,19.9375 22.4375,19.517767 22.4375,19 C22.4375,18.482233 22.017767,18.0625 21.5,18.0625 L20.25,18.0625 C20.077411,18.0625 19.9375,17.922589 19.9375,17.75 L19.9375,16.5 C19.9375,15.982233 19.517767,15.5625 19,15.5625 C18.482233,15.5625 18.0625,15.982233 18.0625,16.5 L18.0625,17.75 C18.0625,17.922589 17.922589,18.0625 17.75,18.0625 L16.5,18.0625 C15.982233,18.0625 15.5625,18.482233 15.5625,19 C15.5625,19.517767 15.982233,19.9375 16.5,19.9375 L17.75,19.9375 C17.922589,19.9375 18.0625,20.077411 18.0625,20.25 L18.0625,21.5 C18.0625,22.017767 18.482233,22.4375 19,22.4375 C19.517767,22.4375 19.9375,22.017767 19.9375,21.5 L19.9375,20.25 C19.9375,20.077411 20.077411,19.9375 20.25,19.9375 L21.5,19.9375 Z M5,2 C3.34314575,2 2,3.34314575 2,5 C2,6.65685425 3.34314575,8 5,8 L19,8 C20.6568542,8 22,6.65685425 22,5 C22,3.34314575 20.6568542,2 19,2 L5,2 Z M5,0 L19,0 C21.7614237,-5.07265313e-16 24,2.23857625 24,5 C24,7.76142375 21.7614237,10 19,10 L5,10 C2.23857625,10 3.38176876e-16,7.76142375 0,5 C-1.2263553e-15,2.23857625 2.23857625,5.07265313e-16 5,0 Z"
                />
              </svg>
            </span>
          </button>
          <button
            class="menubar__button"
            type="button"
            title="Add Row After"
            @click="editor.chain().focus().addRowAfter().run()"
          >
            <span class="svg-icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                :width="iconSize"
                :height="iconSize"
                viewBox="0 0 24 24"
              >
                <path
                  d="M19,0 C21.7600532,0.00330611633 23.9966939,2.23994685 24,5 C24,7.76142375 21.7614237,10 19,10 C16.2385763,10 14,7.76142375 14,5 C14,2.23857625 16.2385763,0 19,0 Z M21.5,5.9375 C22.017767,5.9375 22.4375,5.51776695 22.4375,5 C22.4375,4.48223305 22.017767,4.0625 21.5,4.0625 L20.25,4.0625 C20.077411,4.0625 19.9375,3.92258898 19.9375,3.75 L19.9375,2.5 C19.9375,1.98223305 19.517767,1.5625 19,1.5625 C18.482233,1.5625 18.0625,1.98223305 18.0625,2.5 L18.0625,3.75 C18.0625,3.92258898 17.922589,4.0625 17.75,4.0625 L16.5,4.0625 C15.982233,4.0625 15.5625,4.48223305 15.5625,5 C15.5625,5.51776695 15.982233,5.9375 16.5,5.9375 L17.75,5.9375 C17.922589,5.9375 18.0625,6.07741102 18.0625,6.25 L18.0625,7.5 C18.0625,8.01776695 18.482233,8.4375 19,8.4375 C19.517767,8.4375 19.9375,8.01776695 19.9375,7.5 L19.9375,6.25 C19.9375,6.07741102 20.077411,5.9375 20.25,5.9375 L21.5,5.9375 Z M5,16 C3.34314575,16 2,17.3431458 2,19 C2,20.6568542 3.34314575,22 5,22 L19,22 C20.6568542,22 22,20.6568542 22,19 C22,17.3431458 20.6568542,16 19,16 L5,16 Z M5,14 L19,14 C21.7614237,14 24,16.2385763 24,19 C24,21.7614237 21.7614237,24 19,24 L5,24 C2.23857625,24 3.38176876e-16,21.7614237 0,19 C-1.2263553e-15,16.2385763 2.23857625,14 5,14 Z"
                />
              </svg>
            </span>
          </button>
          <button
            class="menubar__button"
            type="button"
            title="Delete Row"
            @click="editor.chain().focus().deleteRow().run()"
          >
            <span class="svg-icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                :width="iconSize"
                :height="iconSize"
                viewBox="0 0 24 24"
              >
                <path
                  d="M13.2546893,15 C12.8333806,15.6040072 12.5048858,16.2775651 12.2898787,17 L5,17 C2.23857625,17 3.38176876e-16,14.7614237 0,12 C-1.2263553e-15,9.23857625 2.23857625,7 5,7 L19,7 C21.7614237,7 24,9.23857625 24,12 C24,12.6294813 23.8836754,13.2317936 23.6713497,13.7866134 C23.1547532,13.3234155 22.5689168,12.9358807 21.9312708,12.6414391 C21.9762852,12.4347751 22,12.220157 22,12 C22,10.3431458 20.6568542,9 19,9 L5,9 C3.34314575,9 2,10.3431458 2,12 C2,13.6568542 3.34314575,15 5,15 L13.2546893,15 Z M19,14 C21.7600532,14.0033061 23.9966939,16.2399468 24,19 C24,21.7614237 21.7614237,24 19,24 C16.2385763,24 14,21.7614237 14,19 C14,16.2385763 16.2385763,14 19,14 Z M16.5,19.9375 L21.5,19.9375 C22.017767,19.9375 22.4375,19.517767 22.4375,19 C22.4375,18.482233 22.017767,18.0625 21.5,18.0625 L16.5,18.0625 C15.982233,18.0625 15.5625,18.482233 15.5625,19 C15.5625,19.517767 15.982233,19.9375 16.5,19.9375 Z"
                />
              </svg>
            </span>
          </button>

          <label for="table-background-color">
            <i class="fa-solid fa-fill"></i>
            <input
              id="table-background-color"
              type="color"
              style="display: none"
              @input="
                editor
                  .chain()
                  .focus()
                  .setCellAttribute('backgroundColor', $event.target.value)
                  .run()
              "
              value="#ffffff"
            />
          </label>
          <button
            class="menubar__button"
            type="button"
            title="Merge"
            @click="editor.chain().focus().mergeCells().run()"
          >
            <span class="svg-icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                :width="iconSize"
                :height="iconSize"
                viewBox="0 0 24 24"
              >
                <path
                  id="merge-icon"
                  fill="none"
                  stroke="black"
                  stroke-width="1"
                  d="M 0.00,0.00 C 0.00,0.00 24.00,0.00 24.00,0.00 24.00,0.00 24.00,24.00 24.00,24.00 24.00,24.00 0.00,24.00 0.00,24.00 0.00,24.00 0.00,0.00 0.00,0.00 Z M 11.56,0.89 C 11.56,0.89 0.89,0.89 0.89,0.89 0.89,0.89 0.89,8.00 0.89,8.00 0.89,8.00 11.56,8.00 11.56,8.00 11.56,8.00 11.56,0.89 11.56,0.89 Z M 23.11,0.89 C 23.11,0.89 12.44,0.89 12.44,0.89 12.44,0.89 12.44,8.00 12.44,8.00 12.44,8.00 23.11,8.00 23.11,8.00 23.11,8.00 23.11,0.89 23.11,0.89 Z M 23.11,8.89 C 23.11,8.89 0.89,8.89 0.89,8.89 0.89,8.89 0.89,15.11 0.89,15.11 0.89,15.11 23.11,15.11 23.11,15.11 23.11,15.11 23.11,8.89 23.11,8.89 Z M 11.56,16.00 C 11.56,16.00 0.89,16.00 0.89,16.00 0.89,16.00 0.89,23.11 0.89,23.11 0.89,23.11 11.56,23.11 11.56,23.11 11.56,23.11 11.56,16.00 11.56,16.00 Z M 23.11,16.00 C 23.11,16.00 12.44,16.00 12.44,16.00 12.44,16.00 12.44,23.11 12.44,23.11 12.44,23.11 23.11,23.11 23.11,23.11 23.11,23.11 23.11,16.00 23.11,16.00 Z"
                />
              </svg>
            </span>
          </button>

          <button
            class="menubar__button"
            type="button"
            title="Split"
            @click="editor.chain().focus().splitCell().run()"
          >
            <span class="svg-icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                :width="iconSize"
                :height="iconSize"
                viewBox="0 0 24 24"
              >
                <path
                  id="split-icon"
                  fill="none"
                  stroke="black"
                  stroke-width="1"
                  d="M 0.00,0.00 C 0.00,0.00 24.00,0.00 24.00,0.00 24.00,0.00 24.00,24.00 24.00,24.00 24.00,24.00 0.00,24.00 0.00,24.00 0.00,24.00 0.00,0.00 0.00,0.00 Z M 23.00,1.00 C 23.00,1.00 1.00,1.00 1.00,1.00 1.00,1.00 1.00,8.00 1.00,8.00 1.00,8.00 23.00,8.00 23.00,8.00 23.00,8.00 23.00,1.00 23.00,1.00 Z M 5.00,9.00 C 5.00,9.00 1.00,9.00 1.00,9.00 1.00,9.00 1.00,15.00 1.00,15.00 1.00,15.00 5.00,15.00 5.00,15.00 5.00,15.00 5.00,9.00 5.00,9.00 Z M 18.00,9.00 C 18.00,9.00 6.00,9.00 6.00,9.00 6.00,9.00 6.00,15.00 6.00,15.00 6.00,15.00 18.00,15.00 18.00,15.00 18.00,15.00 18.00,9.00 18.00,9.00 Z M 23.00,9.00 C 23.00,9.00 19.00,9.00 19.00,9.00 19.00,9.00 19.00,15.00 19.00,15.00 19.00,15.00 23.00,15.00 23.00,15.00 23.00,15.00 23.00,9.00 23.00,9.00 Z M 23.00,16.00 C 23.00,16.00 1.00,16.00 1.00,16.00 1.00,16.00 1.00,23.00 1.00,23.00 1.00,23.00 23.00,23.00 23.00,23.00 23.00,23.00 23.00,16.00 23.00,16.00 Z"
                />
              </svg>
            </span>
          </button>
          <button
            class="menubar__button"
            type="button"
            title="Add Border"
            @click="addBorder"
            :disabled="editor.getAttributes('table').class.includes('table-bordered')"
            :class="{
              'is-active': editor.getAttributes('table').class.includes('table-bordered')
            }"
          >
            <span class="svg-icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                :width="iconSize"
                :height="iconSize"
                viewBox="0 0 24 24"
              >
                <path
                  id="Selection"
                  fill="none"
                  stroke="black"
                  stroke-width="1"
                  d="M 0.00,0.00 C 0.00,0.00 24.00,0.00 24.00,0.00 24.00,0.00 24.00,24.00 24.00,24.00 24.00,24.00 0.00,24.00 0.00,24.00 0.00,24.00 0.00,0.00 0.00,0.00 Z M 12.00,1.00 C 12.00,1.00 1.00,1.00 1.00,1.00 1.00,1.00 1.00,11.00 1.00,11.00 1.00,11.00 12.00,11.00 12.00,11.00 12.00,11.00 12.00,1.00 12.00,1.00 Z M 23.00,1.00 C 23.00,1.00 13.00,1.00 13.00,1.00 13.00,1.00 13.00,11.00 13.00,11.00 13.00,11.00 23.00,11.00 23.00,11.00 23.00,11.00 23.00,1.00 23.00,1.00 Z M 12.00,12.00 C 12.00,12.00 1.00,12.00 1.00,12.00 1.00,12.00 1.00,23.00 1.00,23.00 1.00,23.00 12.00,23.00 12.00,23.00 12.00,23.00 12.00,12.00 12.00,12.00 Z M 23.00,12.00 C 23.00,12.00 13.00,12.00 13.00,12.00 13.00,12.00 13.00,23.00 13.00,23.00 13.00,23.00 23.00,23.00 23.00,23.00 23.00,23.00 23.00,12.00 23.00,12.00 Z"
                />
              </svg>
            </span>
          </button>
          <button
            class="menubar__button"
            type="button"
            title="Remove Border"
            @click="removeBorder"
            :disabled="!editor.getAttributes('table').class.includes('table-bordered')"
            :class="{
              'is-active': !editor.getAttributes('table').class.includes('table-bordered')
            }"
          >
            <span class="svg-icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                :width="iconSize"
                :height="iconSize"
                viewBox="0 0 24 24"
              >
                <path
                  id="split-icon"
                  fill="none"
                  stroke="black"
                  stroke-width="1"
                  d="M 0.00,0.00 C 0.00,0.00 1.00,0.00 1.00,0.00 1.00,0.00 0.00,1.00 0.00,1.00 0.00,1.00 0.00,0.00 0.00,0.00 Z M 6.00,0.00 C 6.00,0.00 7.00,0.00 7.00,0.00 7.00,0.00 6.00,1.00 6.00,1.00 6.00,1.00 6.00,0.00 6.00,0.00 Z M 11.00,0.00 C 11.00,0.00 12.00,0.00 12.00,0.00 12.00,0.00 11.00,1.00 11.00,1.00 11.00,1.00 11.00,0.00 11.00,0.00 Z M 17.00,0.00 C 17.00,0.00 18.00,0.00 18.00,0.00 18.00,0.00 17.00,1.00 17.00,1.00 17.00,1.00 17.00,0.00 17.00,0.00 Z M 23.00,0.00 C 23.00,0.00 24.00,0.00 24.00,0.00 24.00,0.00 23.00,1.00 23.00,1.00 23.00,1.00 23.00,0.00 23.00,0.00 Z M 0.00,6.00 C 0.00,6.00 1.00,6.00 1.00,6.00 1.00,6.00 0.00,7.00 0.00,7.00 0.00,7.00 0.00,6.00 0.00,6.00 Z M 23.00,6.00 C 23.00,6.00 24.00,6.00 24.00,6.00 24.00,6.00 23.00,7.00 23.00,7.00 23.00,7.00 23.00,6.00 23.00,6.00 Z M 0.00,12.00 C 0.00,12.00 1.00,12.00 1.00,12.00 1.00,12.00 0.00,13.00 0.00,13.00 0.00,13.00 0.00,12.00 0.00,12.00 Z M 23.00,12.00 C 23.00,12.00 24.00,12.00 24.00,12.00 24.00,12.00 23.00,13.00 23.00,13.00 23.00,13.00 23.00,12.00 23.00,12.00 Z M 0.00,18.00 C 0.00,18.00 1.00,18.00 1.00,18.00 1.00,18.00 0.00,19.00 0.00,19.00 0.00,19.00 0.00,18.00 0.00,18.00 Z M 23.00,18.00 C 23.00,18.00 24.00,18.00 24.00,18.00 24.00,18.00 23.00,19.00 23.00,19.00 23.00,19.00 23.00,18.00 23.00,18.00 Z M 0.00,23.00 C 0.00,23.00 1.00,23.00 1.00,23.00 1.00,23.00 0.00,24.00 0.00,24.00 0.00,24.00 0.00,23.00 0.00,23.00 Z M 6.00,23.00 C 6.00,23.00 7.00,23.00 7.00,23.00 7.00,23.00 6.00,24.00 6.00,24.00 6.00,24.00 6.00,23.00 6.00,23.00 Z M 11.00,23.00 C 11.00,23.00 12.00,23.00 12.00,23.00 12.00,23.00 11.00,24.00 11.00,24.00 11.00,24.00 11.00,23.00 11.00,23.00 Z M 17.00,23.00 C 17.00,23.00 18.00,23.00 18.00,23.00 18.00,23.00 17.00,24.00 17.00,24.00 17.00,24.00 17.00,23.00 17.00,23.00 Z M 23.00,23.00 C 23.00,23.00 24.00,23.00 24.00,23.00 24.00,23.00 23.00,24.00 23.00,24.00 23.00,24.00 23.00,23.00 23.00,23.00 Z"
                />
              </svg>
            </span>
          </button>
        </span>
      </div>
    </div>
    <editor-content class="editor__content ps-2" :editor="editor" />
    <input type="hidden" name="content" v-model="html" />
  </div>
</template>

<script>
import { Editor, EditorContent } from '@tiptap/vue-3';
import StarterKit from '@tiptap/starter-kit';
import Blockquote from '@tiptap/extension-blockquote';
import Link from '@tiptap/extension-link';
import Image from '@tiptap/extension-image';
import Table from '@tiptap/extension-table';
import TableRow from '@tiptap/extension-table-row';
import TableCell from '@tiptap/extension-table-cell';
import TableHeader from '@tiptap/extension-table-header';
import ListItem from '@tiptap/extension-list-item';
import TextStyle from '@tiptap/extension-text-style';
import Color from '@tiptap/extension-color';
import TextAlign from '@tiptap/extension-text-align';

const CustomTableCell = TableCell.extend({
  addAttributes() {
    return {
      // extend the existing attributes …
      ...this.parent?.(),

      // and add a new one …
      backgroundColor: {
        default: null,
        parseHTML: element => element.getAttribute('data-background-color'),
        renderHTML: attributes => {
          return {
            'data-background-color': attributes.backgroundColor,
            style: `background-color: ${attributes.backgroundColor}`
          };
        }
      }
    };
  }
});

const CustomTableHeader = TableHeader.extend({
  addAttributes() {
    return {
      // extend the existing attributes …
      ...this.parent?.(),

      // and add a new one …
      backgroundColor: {
        default: null,
        parseHTML: element => element.getAttribute('data-background-color'),
        renderHTML: attributes => {
          return {
            'data-background-color': attributes.backgroundColor,
            style: `background-color: ${attributes.backgroundColor}`
          };
        }
      }
    };
  }
});

const CustomTable = Table.extend({
  addAttributes() {
    return {
      ...this.parent?.(),

      class: {
        default: 'table table-bordered',
        parseHTML: element => element.getAttribute('class'),
        renderHTML: attributes => {
          if (attributes.class) {
            return {
              class: attributes.class
            };
          }
        }
      }
    };
  }
});

export default {
  name: 'managed-content-editor',
  components: {
    EditorContent
  },
  props: {
    content: {
      type: String,
      required: true
    },
    editable: {
      type: Boolean,
      required: false,
      default: true
    },
    iconSize: {
      type: Number,
      required: false,
      default: 16
    }
  },
  data() {
    return {
      editor: null,
      html: this.content
    };
  },
  mounted() {
    this.editor = new Editor({
      editable: this.editable,
      content: this.content,
      extensions: [
        StarterKit,
        Blockquote.configure({
          HTMLAttributes: {
            class: 'blockquote'
          }
        }),
        Link.configure({ openOnClick: false }),
        Image,
        CustomTable.configure({
          resizable: true,
          HTMLAttributes: {
            class: 'managed-content-table table'
          }
        }),
        TableRow,
        CustomTableHeader,
        CustomTableCell,
        Color.configure({ types: [TextStyle.name, ListItem.name] }),
        TextStyle.configure({ types: [ListItem.name] }),
        TextAlign.configure({
          types: ['heading', 'paragraph', 'table']
        })
      ],
      onUpdate: () => {
        this.html = this.editor.getHTML();
      }
    });
  },
  methods: {
    setLink() {
      const previousUrl = this.editor.getAttributes('link').href;
      const url = window.prompt('URL', previousUrl);
      // cancelled
      if (url === null) {
        return;
      }
      // empty
      if (url === '') {
        this.editor.chain().focus().extendMarkRange('link').unsetLink().run();
        return;
      }
      // update link
      this.editor.chain().focus().extendMarkRange('link').setLink({ href: url }).run();
    },
    addImage() {
      const url = window.prompt('Enter the url of your image here');
      if (url) {
        this.editor.chain().focus().setImage({ src: url }).run();
      }
    },
    addBorder() {
      this.editor
        .chain()
        .focus()
        .updateAttributes('table', {
          class: 'table-bordered'
        })
        .run();
    },
    removeBorder() {
      this.editor
        .chain()
        .focus()
        .updateAttributes('table', {
          class: 'table-borderless'
        })
        .run();
    }
  },
  beforeUnmount() {
    this.editor.destroy();
  }
};
</script>
