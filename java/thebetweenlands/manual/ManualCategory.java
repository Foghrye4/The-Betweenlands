package thebetweenlands.manual;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bart on 23/11/2015.
 */
public class ManualCategory {
    public List<Page> pages = new ArrayList<>();
    public List<Page> visiblePages = new ArrayList<>();

    public Page blankPage = new Page("blank", false, ManualManager.EnumManual.GUIDEBOOK);
    public Page currentPageLeft = null;
    public Page currentPageRight = null;
    public int currentPage = 1;
    public int indexPages = 0;
    public int number;
    public String name;

    public ManualCategory(ArrayList<Page> pages, int number, ManualManager.EnumManual manualType, String name) {
        int pageNumber = 1;
        ArrayList<Page> buttonPages = new ArrayList<>();
        ArrayList<Page> tempPages = new ArrayList<>();
        for (Page page : pages) {
            page.setPageNumber(pageNumber);
            if (page.isParent)
                buttonPages.add(page);
            tempPages.add(page);
            pageNumber++;
        }
        ArrayList<Page> buttonPagesNew;
        buttonPagesNew = PageCreators.pageCreatorButtons(buttonPages, manualType);
        indexPages = buttonPagesNew.size();
        this.pages.addAll(buttonPagesNew);
        this.pages.addAll(tempPages);
        this.number = number;
        this.name = name;
    }

    public ManualCategory(ArrayList<Page> pages, ArrayList<Page> introPages, int number, ManualManager.EnumManual manualType, String name) {
        int pageNumber = 1;
        ArrayList<Page> buttonPages = new ArrayList<>();
        ArrayList<Page> tempPages = new ArrayList<>();
        for (Page page : pages) {
            page.setPageNumber(pageNumber);
            if (page.isParent)
                buttonPages.add(page);
            tempPages.add(page);
            pageNumber++;
        }
        ArrayList<Page> buttonPagesNew;
        buttonPagesNew = PageCreators.pageCreatorButtons(buttonPages, manualType);
        indexPages = buttonPagesNew.size() + introPages.size();
        this.pages.addAll(introPages);
        this.pages.addAll(buttonPagesNew);
        this.pages.addAll(tempPages);
        this.number = number;
        this.name = name;
    }


    public void init(GuiManualBase manual, boolean force) {
        if (currentPageLeft == null || currentPageRight == null || force) {
            visiblePages.clear();
            for (Page page : pages)
                if (!page.isHidden || ManualManager.hasFoundPage(manual.player, page.unlocalizedPageName, manual.manualType))
                    visiblePages.add(page);
            if (!visiblePages.isEmpty()) {
                currentPageLeft = this.visiblePages.get(0);
                if (this.visiblePages.size() > 1)
                    currentPageRight = this.visiblePages.get(1);
                else
                    currentPageRight = blankPage;
                currentPageRight.setPageToRight();
            }
        }
        if (currentPageLeft != null)
            currentPageLeft.init(manual);
        if (currentPageRight != null) {
            currentPageRight.init(manual);
        }
    }


    public void setPage(int pageNumber, GuiManualBase manual) {
        if (currentPageLeft != null && currentPageRight != null) {
            if (pageNumber % 2 == 0)
                pageNumber--;
            if (pageNumber <= visiblePages.size()) {
                currentPageLeft = visiblePages.get(pageNumber - 1);
                if (visiblePages.size() >= pageNumber + 1)
                    currentPageRight = visiblePages.get(pageNumber);
                else
                    currentPageRight = blankPage;
                currentPage = pageNumber;
            }
            currentPageLeft.init(manual);
            currentPageRight.init(manual);
            currentPageRight.setPageToRight();

            currentPageLeft.resize();
            currentPageRight.resize();
        }
    }

    public void nextPage(GuiManualBase manual) {
        if (currentPage + 2 <= visiblePages.size()) {
            setPage(currentPage + 2, manual);
        }
    }

    public void previousPage(GuiManualBase manual) {
        if (currentPage - 2 >= 1) {
            setPage(currentPage - 2, manual);
        }
    }


    public void draw(int mouseX, int mouseY) {
        if (currentPageLeft != null)
            currentPageLeft.draw(mouseX, mouseY);
        if (currentPageRight != null)
            currentPageRight.draw(mouseX, mouseY);
    }

    public void keyTyped(char c, int key) {
        if (currentPageLeft != null)
            currentPageLeft.keyTyped(c, key);
        if (currentPageRight != null)
            currentPageRight.keyTyped(c, key);
    }

    public void mouseClicked(int x, int y, int button) {
        if (currentPageLeft != null)
            currentPageLeft.mouseClicked(x, y, button);
        if (currentPageRight != null)
            currentPageRight.mouseClicked(x, y, button);
    }

    public void updateScreen() {
        if (currentPageLeft != null)
            currentPageLeft.updateScreen();
        if (currentPageRight != null)
            currentPageRight.updateScreen();
    }

}